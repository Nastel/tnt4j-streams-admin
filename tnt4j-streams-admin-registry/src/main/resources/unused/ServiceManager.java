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

import java.io.IOException;
import java.nio.charset.Charset;

import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;

/**
 * The type Service manager.
 */
public class ServiceManager {

	/**
	 * The Curator framework.
	 */
	CuratorFramework curatorFramework;

	/**
	 * Instantiates a new Service manager.
	 *
	 * @param curatorFramework
	 *            the curator framework
	 */
	public ServiceManager(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	/**
	 * Register or update offered service.
	 *
	 * @param offeredServicesPath
	 *            the offered services path
	 * @param pathToStaticData
	 *            the path to static data
	 * @param serviceName
	 *            the service name
	 */
	public void registerOrUpdateOfferedService(String offeredServicesPath, String pathToStaticData,
			String serviceName) {
		String servicePath = offeredServicesPath + serviceName;

		String staticServiceData = null;
		try {
			staticServiceData = FileUtils.readFile(pathToStaticData, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (CuratorUtils.doesNodeExist(servicePath, curatorFramework)) {
			CuratorUtils.setData(servicePath, staticServiceData, curatorFramework);
		} else {
			CuratorUtils.createNode(servicePath, curatorFramework);
			CuratorUtils.setData(servicePath, staticServiceData, curatorFramework);
		}
	}

}
