import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.List;
import java.util.Collections;

public class TestAcl {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("test_acl.txt");
        Files.write(path, "Hello".getBytes());

        UserPrincipal owner = Files.getOwner(path);
        System.out.println("Owner: " + owner.getName());

        AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (aclView == null) {
            System.out.println("ACL not supported");
            return;
        }

        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, 
                                AclEntryPermission.APPEND_DATA, AclEntryPermission.READ_NAMED_ATTRS,
                                AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.EXECUTE,
                                AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES,
                                AclEntryPermission.DELETE, AclEntryPermission.READ_ACL, AclEntryPermission.SYNCHRONIZE)
                .build();

        aclView.setAcl(Collections.singletonList(entry));
        System.out.println("ACL set successfully");
    }
}
