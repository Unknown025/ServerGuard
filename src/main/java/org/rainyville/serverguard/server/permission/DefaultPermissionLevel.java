package org.rainyville.serverguard.server.permission;

public enum DefaultPermissionLevel {
    /**
     * Anyone can access this command.
     */
    ALL,
    /**
     * Only server operators can access this command.
     */
    OP,
    /**
     * No one can access this command.
     */
    NONE
}
