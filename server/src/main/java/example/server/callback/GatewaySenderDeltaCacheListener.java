package example.server.callback;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Operation;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.util.CacheListenerAdapter;

import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.EnumListenerEvent;
import org.apache.geode.internal.cache.PartitionedRegion;

import org.apache.geode.internal.cache.wan.AbstractGatewaySender;
import org.apache.geode.internal.cache.wan.GatewaySenderEventCallbackArgument;
import org.apache.geode.logging.internal.log4j.api.LogService;

import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;

public class GatewaySenderDeltaCacheListener extends CacheListenerAdapter {

	public static final String proxyRegionName = "_gateway_sender_delta_proxy";

	public void afterCreate(EntryEvent event) {
		process(event);
	}

	public void afterUpdate(EntryEvent event) {
		process(event);
	}

	public void afterDestroy(EntryEvent event) {
		process(event);
	}
	
	private void process(EntryEvent event) {
	  EntryEventImpl eei = (EntryEventImpl) event;
		if (!isFromRemoteWANSite(eei)) {
      // Get the GatewaySender proxy region
      PartitionedRegion proxyRegion = (PartitionedRegion) getProxyRegion(event.getRegion());
    
      // Create the appropriate event
      EntryEventImpl proxyEvent = createProxyEntryEvent(eei, proxyRegion);

      // Add the event to any GatewaySender queues
      deliverToGatewaySenderQueues(proxyEvent);
    }
  }
	
	private boolean isFromRemoteWANSite(EntryEventImpl event) {
    return event.getRawCallbackArgument() instanceof GatewaySenderEventCallbackArgument;
	}

  private EntryEventImpl createProxyEntryEvent(EntryEventImpl event, PartitionedRegion proxyRegion) {
	  // Get the event's bytes and operation
    byte[] newValue = null;
    boolean isDelta;
    Operation operation;
    if (event.getDeltaBytes() != null) {
      newValue = event.getDeltaBytes();
      operation = Operation.UPDATE;
      isDelta = true;
    } else if (event.getCachedSerializedNewValue() != null) {
      newValue = event.getCachedSerializedNewValue();
      operation = Operation.CREATE;
      isDelta = false;
    } else {
      operation = Operation.DESTROY;
      isDelta = false;
    }

    // Create the proxy event with the bytes and operation
    EntryEventImpl proxyEvent = EntryEventImpl.create(proxyRegion, operation, event.getKey(), newValue, isDelta /*callbackArg*/, event.isOriginRemote(), event.getDistributedMember(), false /* generateCallbacks */, event.getEventId());
    proxyEvent.setContext(event.getContext());
    proxyEvent.setVersionTag(event.getVersionTag());
    proxyEvent.setPossibleDuplicate(event.isPossibleDuplicate());
    proxyEvent.setTailKey((Long) event.getCallbackArgument());

    return proxyEvent;
  }

  private void deliverToGatewaySenderQueues(EntryEventImpl wanEvent) {
    Region region = wanEvent.getRegion();
    Cache cache = region.getCache();
    Set<String> senderIds = region.getAttributes().getGatewaySenderIds();
    for (String senderId : senderIds) {
      // Get the GatewaySender
      AbstractGatewaySender sender = (AbstractGatewaySender) cache.getGatewaySender(senderId);

      // Distribute the EntryEvent to the GatewaySender
      sender.distribute(getEnumListenerEvent(wanEvent.getOperation()), wanEvent, getRemoteDsIds(cache, senderIds));
    }
  }

	private Region getProxyRegion (Region region) { 
		return region.getCache().getRegion(region.getName() + proxyRegionName);
	}
	
	private EnumListenerEvent getEnumListenerEvent(Operation operation) {
	  EnumListenerEvent ele = null;
    if (operation.isCreate()) {
      ele = EnumListenerEvent.AFTER_CREATE;
    } else if (operation.isUpdate()) {
      ele = EnumListenerEvent.AFTER_UPDATE;
    } else if (operation.isDestroy()) {
      ele = EnumListenerEvent.AFTER_DESTROY;
    }
    return ele;
	}

  private List<Integer> getRemoteDsIds(Cache cache, Set<String> senderIds) {
    List<Integer> remoteDsIds = senderIds.stream()
      .map(cache::getGatewaySender)
      .map(sender -> sender.getRemoteDSId())
      .collect(Collectors.toList());
    return remoteDsIds;
  }
}
