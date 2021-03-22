package example.server.callback;

import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.util.CacheWriterAdapter;

import org.apache.geode.internal.cache.BucketRegion;
import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.PartitionedRegion;

import org.apache.geode.internal.cache.wan.GatewaySenderEventCallbackArgument;

public class GatewaySenderDeltaCacheWriter extends CacheWriterAdapter {

	public static final String proxyRegionName = "_gateway_sender_delta_proxy";

	public void beforeCreate(EntryEvent event) throws CacheWriterException {
		process(event);
	}
	
	public void beforeUpdate(EntryEvent event) throws CacheWriterException {
		process(event);
	}
	
	public void beforeDestroy(EntryEvent event) throws CacheWriterException {
		process(event);
	}
	
	private void process(EntryEvent event) {
    EntryEventImpl eei = (EntryEventImpl) event;
		if (!isFromRemoteWANSite(eei)) {
      // Update the tailKey (which is the key in the queue)
      // The tailKey is set by handleWANEvent in the event in the primary.
      // It won't be called in this case since the data region is not WAN-enabled.
      setTailKey(eei);
    
      // Set the callback argument since the tail key is not serialized between members
      // if the region is not wan-enabled.
      eei.setCallbackArgument(eei.getTailKey());
    }
	}
		
	private boolean isFromRemoteWANSite(EntryEventImpl event) {
    return event.getRawCallbackArgument() instanceof GatewaySenderEventCallbackArgument;
	}

	private void setTailKey(EntryEventImpl event) {
	  PartitionedRegion pr = (PartitionedRegion) getProxyRegion(event.getRegion());
	  BucketRegion br = pr.getBucketRegion(event.getKey());
	  br.handleWANEvent(event);
	}

	private Region getProxyRegion (Region region) { 
		return region.getCache().getRegion(region.getName() + proxyRegionName);
	}
}
