package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceMember;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceMemberRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.identity.service.UserBootstrapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBootstrapServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private UserBootstrapService userBootstrapService;

    @Test
    void testCreateUserWithPersonalWorkspace_Success() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);

        User user = userBootstrapService.createUserWithPersonalWorkspace(
                "test@example.com",
                "hashed",
                "Test User",
                "http://pic.jpg",
                Instant.now()
        );

        assertEquals("test@example.com", user.getEmail());
        
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace workspace = workspaceCaptor.getValue();
        assertEquals("Test User's Workspace", workspace.getName());
        assertEquals("test-user", workspace.getSlug());
        
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void testCreateUserWithPersonalWorkspace_NullFullName_NormalEmail() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);

        userBootstrapService.createUserWithPersonalWorkspace(
                "testuser@example.com",
                "hashed",
                null,
                null,
                null
        );
        
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace workspace = workspaceCaptor.getValue();
        assertEquals("testuser's Workspace", workspace.getName());
        assertEquals("testuser-example-com", workspace.getSlug());
    }

    @Test
    void testCreateUserWithPersonalWorkspace_NullFullName_NoAtEmail() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);

        userBootstrapService.createUserWithPersonalWorkspace(
                "testuser",
                "hashed",
                null,
                null,
                null
        );
        
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace workspace = workspaceCaptor.getValue();
        assertEquals("testuser's Workspace", workspace.getName());
    }

    @Test
    void testCreateUserWithPersonalWorkspace_SlugConflict() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(i -> i.getArgument(0));
        
        // Return true for the first attempt, false for the second
        when(workspaceRepository.existsBySlug("test-user")).thenReturn(true);
        when(workspaceRepository.existsBySlug(argThat(s -> s.startsWith("test-user-")))).thenReturn(false);

        userBootstrapService.createUserWithPersonalWorkspace(
                "test@example.com",
                "hashed",
                "Test User",
                null,
                null
        );
        
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace workspace = workspaceCaptor.getValue();
        assertTrue(workspace.getSlug().startsWith("test-user-"));
    }

    @Test
    void testCreateUserWithPersonalWorkspace_EmptySlugFallback() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);

        userBootstrapService.createUserWithPersonalWorkspace(
                "???", // slugify will make this empty
                "hashed",
                "!!!", // slugify will make this empty
                null,
                null
        );
        
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace workspace = workspaceCaptor.getValue();
        assertEquals("user", workspace.getSlug());
    }
}
