package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server();
    }

    @Test
    public void testCreateRoom() {
        assertTrue(server.createRoom("room"));
        assertFalse(server.createRoom("room"));
    }

    @Test
    public void testAddUser() {
        server.createRoom("room");
        assertTrue(server.addUserToRoom("user", "room"));
        assertFalse(server.addUserToRoom("user", "notRoom"));
    }

    @Test
    public void testGetUsers() {
        server.createRoom("room");
        server.addUserToRoom("u1", "room");
        server.addUserToRoom("u2", "room");
        var users = Set.of("u1", "u2");
        assertEquals(users, server.getUsersInRoom("room"));
    }

    @Test
    public void testRemoveUser() {
        server.createRoom("room");
        server.addUserToRoom("user", "room");
        server.removeUserFromRoom("user", "room");
        var users = server.getUsersInRoom("room");
        assertFalse(users.contains("user"));
    }

    @Test
    public void testGetRooms() {
        server.createRoom("1");
        server.createRoom("2");
        server.createRoom("3");
        var rooms = Set.of("1", "2", "3");
        assertEquals(rooms, server.getAllRooms());
    }
}
