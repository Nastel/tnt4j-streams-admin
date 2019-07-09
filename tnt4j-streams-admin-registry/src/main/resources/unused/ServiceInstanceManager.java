/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unused;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

/**
 * The type Service instance manager.
 */
public class ServiceInstanceManager {

	private Map<String, ServiceDiscovery<String>> serviceNameToServiceDiscovery;

	/**
	 * Instantiates a new Service instance manager.
	 *
	 * @param serviceNameToServiceDiscovery
	 *            the service name to service discovery
	 */
	public ServiceInstanceManager(Map<String, ServiceDiscovery<String>> serviceNameToServiceDiscovery) {
		this.serviceNameToServiceDiscovery = serviceNameToServiceDiscovery;
	}

	private ServiceInstance<String> createServiceInstance(Properties serviceInstanceProperties) {

		UriSpec uriSpec = new UriSpec(serviceInstanceProperties.getProperty("uriSpec"));

		String serviceName = serviceInstanceProperties.getProperty("name");
		int port = parseInt(serviceInstanceProperties.getProperty("port"));
		String address = serviceInstanceProperties.getProperty("address");
		String payload = serviceInstanceProperties.getProperty("payload");

		ServiceInstance<String> service = null;
		try {
			service = ServiceInstance.<String> builder().name(serviceName).port(port).payload(payload).uriSpec(uriSpec)
					.address(address).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return service;
	}

	private ServiceDiscovery<String> createServiceDiscovery(ServiceInstance<String> serviceInstance, String basePath,
			CuratorFramework curatorFramework) {

		ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class)
				.client(curatorFramework).basePath(basePath).thisInstance(serviceInstance).watchInstances(true).build();
		return serviceDiscovery;
	}

	/**
	 * Create service.
	 *
	 * @param serviceInstanceProperties
	 *            the service instance properties
	 * @param curatorFramework
	 *            the curator framework
	 */
	public void createService(Properties serviceInstanceProperties, CuratorFramework curatorFramework) {
		String serviceName = serviceInstanceProperties.getProperty("name");
		String serviceBasePath = serviceInstanceProperties.getProperty("basePath");

		ServiceInstance<String> serviceInstance = createServiceInstance(serviceInstanceProperties);
		ServiceDiscovery<String> serviceDiscovery = createServiceDiscovery(serviceInstance, serviceBasePath,
				curatorFramework);

		serviceNameToServiceDiscovery.put(serviceName, serviceDiscovery);
	}

	/**
	 * Start service.
	 *
	 * @param serviceName
	 *            the service name
	 */
	public void startService(String serviceName) {
		ServiceDiscovery<String> service = serviceNameToServiceDiscovery.get(serviceName);

		try {
			service.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close service.
	 *
	 * @param serviceName
	 *            the service name
	 */
	public void closeService(String serviceName) {
		ServiceDiscovery<String> service = serviceNameToServiceDiscovery.get(serviceName);

		try {
			service.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start all services.
	 */
	public void startAllServices() {
		Set<String> services = serviceNameToServiceDiscovery.keySet();

		for (String service : services) {
			startService(service);
		}
	}

	/**
	 * Close all services.
	 */
	public void closeAllServices() {
		Set<String> services = serviceNameToServiceDiscovery.keySet();

		for (String service : services) {
			closeService(service);
		}

	}

}
