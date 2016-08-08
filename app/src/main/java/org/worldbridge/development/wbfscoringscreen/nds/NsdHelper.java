package org.worldbridge.development.wbfscoringscreen.nds;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class NsdHelper {

    private Handler mNdsEventHandler;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    private AtomicBoolean running;
    private String serviceType;

    public NsdHelper(NsdManager mNsdManager, Handler mNdsEventHandler, String serviceType) {
        this.mNdsEventHandler = mNdsEventHandler;
        this.mNsdManager = mNsdManager;
        this.serviceType = serviceType;

        running = new AtomicBoolean(false);
    }

    public void discoveryStart() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                Log.i(this.getClass().getSimpleName(), "onStartDiscoveryFailed");
                Message m = createInfoMessage("onStartDiscoveryFailed");

                if (mNdsEventHandler != null) {
                    mNdsEventHandler.sendMessage(m);
                }
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                Log.i(this.getClass().getSimpleName(), "onStopDiscoveryFailed");
                Message m = createInfoMessage("onStopDiscoveryFailed");

                if (mNdsEventHandler != null) {
                    mNdsEventHandler.sendMessage(m);
                }
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Log.i(this.getClass().getSimpleName(), "onDiscoveryStarted");

                if (running.compareAndSet(false, true)) {
                    Bundle b = new Bundle();
                    b.putString("messageid", MessageType.MESSAGE_DISCOVERY_START.toString());
                    b.putString("type", s);
                    Message m = new Message();
                    m.setData(b);

                    if (mNdsEventHandler != null) {
                        mNdsEventHandler.sendMessage(m);
                    }
                }
            }

            @Override
            public void onDiscoveryStopped(String s) {
                Log.i(this.getClass().getSimpleName(), "onDiscoveryStopped");

                if (running.compareAndSet(true, false)) {

                    Bundle b = new Bundle();
                    b.putString("messageid", MessageType.MESSAGE_DISCOVERY_STOP.toString());
                    b.putString("type", s);
                    Message m = new Message();
                    m.setData(b);

                    if (mNdsEventHandler != null) {
                        mNdsEventHandler.sendMessage(m);
                    }
                }
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Log.i(this.getClass().getSimpleName(), "onServiceFound");

                if (nsdServiceInfo.getHost() != null) {
                    Bundle b = new Bundle();
                    b.putString("messageid", MessageType.MESSAGE_SRVFOUND.toString());
                    b.putString("name", nsdServiceInfo.getServiceName());
                    b.putString("type", nsdServiceInfo.getServiceType());
                    b.putString("port", Integer.toString(nsdServiceInfo.getPort()));

                    b.putString("host", nsdServiceInfo.getHost().getHostAddress());
                    Message m = new Message();
                    m.setData(b);
                    if (mNdsEventHandler != null) {
                        mNdsEventHandler.sendMessage(m);
                    }
                } else {
                    doAsyncResolve(nsdServiceInfo);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Log.i(this.getClass().getSimpleName(), "onServiceLost");

                Bundle b = new Bundle();
                b.putString("messageid", MessageType.MESSAGE_SRVLOST.toString());
                b.putString("name", nsdServiceInfo.getServiceName());
                b.putString("type", nsdServiceInfo.getServiceType());
                if (nsdServiceInfo.getHost() != null) {
                    b.putString("host", nsdServiceInfo.getHost().getHostAddress());
                }
                b.putString("port", Integer.toString(nsdServiceInfo.getPort()));
                Message m = new Message();
                m.setData(b);
                if (mNdsEventHandler != null) {
                    mNdsEventHandler.sendMessage(m);
                }

            }
        };
        mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void discoveryStop() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void doAsyncResolve(NsdServiceInfo nsdServiceInfo) {
        mNsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Message m = createInfoMessage("Failed to resolve " + nsdServiceInfo.getServiceName() + "." + nsdServiceInfo.getServiceType());
                if (mNdsEventHandler != null) {
                    mNdsEventHandler.sendMessage(m);
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                Bundle b = new Bundle();
                b.putString("messageid", MessageType.MESSAGE_SRVFOUND.toString());
                b.putString("name", nsdServiceInfo.getServiceName());
                b.putString("type", nsdServiceInfo.getServiceType());
                b.putString("port", Integer.toString(nsdServiceInfo.getPort()));
                b.putString("host", nsdServiceInfo.getHost().getHostAddress());

                Message m = new Message();
                m.setData(b);
                if (mNdsEventHandler != null) {
                    mNdsEventHandler.sendMessage(m);
                }
            }
        });
    }

    public void cleanup() {
        // Nothing yet
    }

    private Message createInfoMessage(String message) {
        Bundle b = new Bundle();
        b.putString("messageid", MessageType.MESSAGE_INFO.toString());
        b.putString("message", message);
        Message m = new Message();
        m.setData(b);
        return m;
    }

    public enum MessageType {
        MESSAGE_SRVLOST,
        MESSAGE_SRVFOUND,
        MESSAGE_INFO,
        MESSAGE_DISCOVERY_START,
        MESSAGE_DISCOVERY_STOP;
    }

}
