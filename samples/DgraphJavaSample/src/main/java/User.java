public class User {
    long expiry;
    String password;
    String slackId;
    String state;
    String email;

    User(final long expiry, final String password, final String slackId,
            final String state, final String email) {
        this.expiry = expiry;
        this.password = password;
        this.slackId = slackId;
        this.state = state;
        this.email = email;
    }

    long getExpiry() {
        return expiry;
    }

    String getPassword() {
        return password;
    }

    String getSlackId() {
        return slackId;
    }

    String getState() {
        return state;
    }

    String getEmail() {
        return email;
    }
}