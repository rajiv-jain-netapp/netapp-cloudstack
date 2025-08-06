package org.apache.cloudstack.storage.lifecycle;


import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.storage.StoragePool;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.ClusterScope;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.HostScope;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreLifeCycle;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreParameters;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.util.NetAppUtil;
import org.apache.cloudstack.storage.volume.datastore.PrimaryDataStoreHelper;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.Map;

public class NetAppPrimaryDatastoreLifecycle implements PrimaryDataStoreLifeCycle {

    private static final Logger s_logger = Logger.getLogger(NetAppPrimaryDatastoreLifecycle.class);

    @Inject
    private PrimaryDataStoreHelper primaryDataStoreHelper;
    /**
     * Creates primary storage on NetApp storage
     * @param dsInfos
     * @return
     */
    @Override
    public DataStore initialize(Map<String, Object> dsInfos) {
        s_logger.info("NetAppPrimaryDatastoreLifecycle: initialize: params: dsInfos: "+ dsInfos);
        String url = (String)dsInfos.get("url");
        Long zoneId = (Long)dsInfos.get("zoneId");
        Long podId = (Long)dsInfos.get("podId");
        Long clusterId = (Long)dsInfos.get("clusterId");
        String storagePoolName = (String)dsInfos.get("name");
        String providerName = (String)dsInfos.get("providerName");
        Long capacityBytes = (Long)dsInfos.get("capacityBytes");
        Long capacityIops = (Long)dsInfos.get("capacityIops");
        String tags = (String)dsInfos.get("tags");
        Boolean isTagARule = (Boolean) dsInfos.get("isTagARule");
        Map<String, String> details = (Map<String, String>)dsInfos.get("details");

        s_logger.info("values: "+dsInfos.toString());

        if (podId == null ^ clusterId == null) {
            throw new CloudRuntimeException("Both POD and cluster values together should either be specified or not.");
        }

        if (url == null || url.trim().equals("")) {
            throw new IllegalArgumentException("Valid 'URL' value must be present.");
        }

        if (capacityBytes == null || capacityBytes <= 0) {
            throw new IllegalArgumentException("'capacityBytes' must be present and greater than 0.");
        }

        if (capacityIops == null || capacityIops <= 0) {
            throw new IllegalArgumentException("'capacityIops' must be present and greater than 0.");
        }

        PrimaryDataStoreParameters parameters = new PrimaryDataStoreParameters();
        NetAppUtil util = new NetAppUtil();
        //util.POST()
        /*
           Instantiating for ONTAP connection would come here.
         */


        // this adds a row in the cloud.storage_pool table for this SolidFire cluster
        return primaryDataStoreHelper.createPrimaryDataStore(parameters);

    }

    @Override
    public boolean attachCluster(DataStore store, ClusterScope scope) {
        return false;
    }

    @Override
    public boolean attachHost(DataStore store, HostScope scope, StoragePoolInfo existingInfo) {
        return false;
    }

    @Override
    public boolean attachZone(DataStore dataStore, ZoneScope scope, Hypervisor.HypervisorType hypervisorType) {
        return false;
    }

    @Override
    public boolean maintain(DataStore store) {
        return true;
    }

    @Override
    public boolean cancelMaintain(DataStore store) {
        return true;
    }

    @Override
    public boolean deleteDataStore(DataStore store) {
        return true;
    }

    @Override
    public boolean migrateToObjectStore(DataStore store) {
        return true;
    }

    @Override
    public void updateStoragePool(StoragePool storagePool, Map<String, String> details) {

    }

    @Override
    public void enableStoragePool(DataStore store) {

    }

    @Override
    public void disableStoragePool(DataStore store) {

    }

    @Override
    public void changeStoragePoolScopeToZone(DataStore store, ClusterScope clusterScope, Hypervisor.HypervisorType hypervisorType) {

    }

    @Override
    public void changeStoragePoolScopeToCluster(DataStore store, ClusterScope clusterScope, Hypervisor.HypervisorType hypervisorType) {

    }
}