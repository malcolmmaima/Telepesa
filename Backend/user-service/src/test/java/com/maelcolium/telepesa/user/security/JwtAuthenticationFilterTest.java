package com.maelcolium.telepesa.user.security;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidJwtToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.validateToken(token)).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        boolean hasUserRole = authentication.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority()));
        assertThat(hasUserRole).isTrue();
    }

    @Test
    void doFilterInternal_WithNoAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithEmptyAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithNonBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithInvalidJwtToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.validateToken(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithBearerOnlyToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithBearerSpaceOnly_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithExceptionInTokenValidation_ShouldContinueFilter() throws ServletException, IOException {
        // Given
        String token = "problematic.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.validateToken(token)).thenThrow(new RuntimeException("Token validation failed"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithExceptionInUserDetailsLoading_ShouldContinueFilter() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.validateToken(token)).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username))
            .thenThrow(new RuntimeException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotOverrideIt() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String username = "testuser";
        
        // Set existing authentication
        UserPrincipal existingUser = new UserPrincipal(1L, "existing", "existing@test.com", 
            "password", List.of(new SimpleGrantedAuthority("ROLE_USER")), true, true);
        SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities()));
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.validateToken(token)).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        // Authentication should be overridden with new token
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    }
}
