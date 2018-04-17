package pl.edu.icm.unicore.portal.openfoam;

import eu.unicore.griddisco.core.model.AbstractAddressableResource;
import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.util.Optional;

public class OpenFOAMGridEnvironment {
    private BrokerService brokerService;
    private StorageFactoryService storageFactoryService;

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public void setBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    public StorageFactoryService getStorageFactoryService() {
        return storageFactoryService;
    }

    public void setStorageFactoryService(StorageFactoryService storageFactoryService) {
        this.storageFactoryService = storageFactoryService;
    }

    @Override
    public String toString() {
        return String.format("OpenFOAMGridEnvironment{brokerService=%s, storageFactoryService=%s}",
                getStringAddress(brokerService), getStringAddress(storageFactoryService)
        );
    }

    private String getStringAddress(AbstractAddressableResource addressableResource) {
        return Optional.ofNullable(addressableResource)
                .map(AbstractAddressableResource::getAddress)
                .map(EndpointReferenceType::getAddress)
                .map(AttributedURIType::getStringValue)
                .orElse("<NONE>");
    }
}
