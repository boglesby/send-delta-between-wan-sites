package example.server.callback;

import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Operation;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.util.CacheWriterAdapter;

import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.LocalRegion;

import org.apache.geode.internal.cache.wan.GatewaySenderEventCallbackArgument;

public class GatewaySenderProxyCacheWriter extends CacheWriterAdapter {

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
		if (isFromRemoteWANSite(eei)) {
	    System.out.println("GatewaySenderProxyCacheWriter.process invoked event=" + event);
      byte[] newValue = (byte[]) eei.getNewValue();
      Operation operation = event.getOperation();
      boolean callbackArg = (Boolean) event.getCallbackArgument();
      if (event.getOperation().isDestroy()) {
        getDataRegion(event.getRegion()).basicBridgeDestroy(event.getKey(), eei.getRawCallbackArgument(), eei.getContext(), false, getClientEvent(eei));
      } else {
        Object value = null;
        byte[] deltaBytes = null;
        boolean isObject = false;
        if (callbackArg) {
          deltaBytes = (byte[]) eei.getNewValue();
        } else {
          value = eei.getNewValue();
          isObject = true;
        }
        getDataRegion(event.getRegion()).basicBridgePut(event.getKey(), value, deltaBytes, isObject, eei.getRawCallbackArgument(), eei.getContext(), false, getClientEvent(eei));
      }
		}
	}

	private LocalRegion getDataRegion(Region region) { 
	  int index = region.getFullPath().indexOf(proxyRegionName);
	  return (LocalRegion) region.getCache().getRegion(region.getFullPath().substring(0, index));
	}
	
	private boolean isFromRemoteWANSite(EntryEventImpl event) {
    return event.getRawCallbackArgument() instanceof GatewaySenderEventCallbackArgument;
	}
	
	private EntryEventImpl getClientEvent(EntryEventImpl event) {
	  return event;
	}
}
