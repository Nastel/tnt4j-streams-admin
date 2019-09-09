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
