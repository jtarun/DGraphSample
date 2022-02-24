import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphGrpc.DgraphStub;
import io.dgraph.DgraphProto;
import io.dgraph.DgraphProto.Mutation;
import io.dgraph.Transaction;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/**
 * CLASS_DEFINITION_COMMENTS
 *
 * @author jainendra tarun (jainendra.tarun@thoughtspot.com)
 */
public class DGraphRepository {

    private static final String TEST_HOSTNAME = "localhost";
    private static final int TEST_PORT = 9080;

    //private static final String TEST_HOSTNAME = "localhost";
    //private static final int TEST_PORT = 32683;

    private static DgraphClient createDgraphClient(boolean withAuthHeader) {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress(TEST_HOSTNAME, TEST_PORT).usePlaintext().build();
        DgraphStub stub = DgraphGrpc.newStub(channel);

        if (withAuthHeader) {
            Metadata metadata = new Metadata();
            metadata.put(
                    Metadata.Key.of("auth-token", Metadata.ASCII_STRING_MARSHALLER), "the-auth-token-value");
            stub = MetadataUtils.attachHeaders(stub, metadata);
        }

        return new DgraphClient(stub);
    }

    private DgraphClient dgraphClient;

    public DGraphRepository(DgraphClient dgraphClient) {
        this.dgraphClient = dgraphClient;
        //this.dgraphClient = createDgraphClient(false);
    }

    /**
     * mutation MyMutation {
     *   addUser(input: {displayName: “tsadmin”, password: “pass”, expiry: “2000”}) {
     *     user {
     *       password
     *       displayName
     *     }
     *   }
     * }
     * @param user
     */

    public void createUser(User user) {
        Gson gson = new Gson(); // For JSON encode/decode
        Transaction txn = dgraphClient.newTransaction();

        try {
            // Create data
            DGraphUser dGraphUser = new DGraphUser(user.getExpiry(), user.getPassword(),
                    user.getSlackId(), user.getState(), user.getEmail());

            // Serialize it
            String json = gson.toJson(dGraphUser);

/*            // Run mutation
            Mutation mutation = Mutation.newBuilder()
                    .setSetJson(ByteString.copyFromUtf8(json)).build();*/



            String triples = "_:luk <email> \"test1@dgraph.io\" .\n" +
                    "_:luk <name> \"test1\" .\n" +
                    "_:luk <dgraph.type> \"User\" .";

            Mutation mutation = Mutation.newBuilder()
                    .setSetNquads(ByteString.copyFromUtf8(triples))
                    .build();

            txn.mutate(mutation);
            txn.commit();

        } finally {
            txn.discard();
        }
    }

    public DGraphUser getUser(String name) {
        String query = "query me($a: string) { me(func: eq(name, $a)) { name }}";
        Map<String, String> vars = Collections.singletonMap("$a", name);
        DgraphProto.Response response =
                dgraphClient.newReadOnlyTransaction().queryRDFWithVars(query, vars);

        String json = response.getJson().toString();
        System.out.println(json);
        return null;
    }

    private static class DGraphOkta {
        String id;
        String type;
    }

    private static class DGraphUser {
        long expiry;
        String password;
        String slackId;
        String state;
        String email;

        DGraphUser(final long expiry, final String password, final String slackId,
                final String state, final String email) {
            this.expiry = expiry;
            this.password = password;
            this.slackId = slackId;
            this.state = state;
            this.email = email;
        }
    }

}
