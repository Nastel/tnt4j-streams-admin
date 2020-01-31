/*
 * Copyright 2014-2020 JKOOL, LLC.
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

package converters;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.zookeeper.cli.AclParser;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.beust.jcommander.IStringConverter;

public class AclConverter implements IStringConverter<ACL> {

	private void hashIds(List<ACL> acls) {
		for (ACL acl : acls) {
			try {
				String digest = DigestAuthenticationProvider.generateDigest(acl.getId().getId());
				acl.setId(new Id("digest", digest));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ACL convert(String s) {
		List<ACL> acls = AclParser.parse(s);
		hashIds(acls);
		return acls.get(0);
	}
}
