# Telepesa: React Dashboard Development Rules

These rules are based on best practices for building modern, scalable React applications using TypeScript and contemporary development patterns.

## Project Structure & Architecture

### Project Structure
- Use feature-based folder organization over layer-based
- Follow this structure:
  ```
  src/
    components/
      ui/           # Reusable UI components
      forms/        # Form components
      layout/       # Layout components
    features/
      auth/         # Authentication feature
      dashboard/    # Dashboard feature
      users/        # User management feature
      payments/     # Payment feature
      reports/      # Reports feature
    hooks/          # Custom React hooks
    lib/            # Utility libraries and configurations
    store/          # State management
    types/          # TypeScript type definitions
    utils/          # Utility functions
    constants/      # Application constants
    assets/         # Static assets
    styles/         # Global styles and themes
  ```

### Feature Organization
- Group related components, hooks, and utilities by feature
- Each feature should contain:
  ```
  features/payments/
    components/     # Feature-specific components
    hooks/         # Feature-specific hooks
    services/      # API calls and business logic
    types/         # Feature-specific types
    utils/         # Feature-specific utilities
    index.ts       # Feature exports
  ```

### Package Naming & Imports
- Use absolute imports with path mapping
- Configure path aliases in `tsconfig.json`:
  ```json
  {
    "compilerOptions": {
      "baseUrl": "src",
      "paths": {
        "@/components/*": ["components/*"],
        "@/features/*": ["features/*"],
        "@/hooks/*": ["hooks/*"],
        "@/lib/*": ["lib/*"],
        "@/store/*": ["store/*"],
        "@/types/*": ["types/*"],
        "@/utils/*": ["utils/*"]
      }
    }
  }
  ```

## TypeScript Conventions

### Type Definitions
- Define strict types for all props, state, and API responses
- Use interfaces for object shapes, types for unions and primitives
- Create domain-specific type files
- Example:
  ```typescript
  // types/user.ts
  export interface User {
    id: string;
    username: string;
    email: string;
    role: UserRole;
    createdAt: string;
    updatedAt: string;
  }

  export type UserRole = 'admin' | 'user' | 'viewer';

  export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    role: UserRole;
  }

  export interface UserResponse {
    success: boolean;
    data: User;
    message?: string;
  }
  ```

### Component Props
- Always type component props explicitly
- Use React.FC sparingly, prefer explicit typing
- Use optional props with default values
- Example:
  ```typescript
  interface UserCardProps {
    user: User;
    onEdit?: (user: User) => void;
    onDelete?: (userId: string) => void;
    showActions?: boolean;
  }

  export const UserCard = ({ 
    user, 
    onEdit, 
    onDelete, 
    showActions = true 
  }: UserCardProps) => {
    // Component implementation
  };
  ```

### API Types
- Define API response and request types
- Use generic types for common patterns
- Example:
  ```typescript
  export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
    errors?: string[];
  }

  export interface PaginatedResponse<T> extends ApiResponse<T[]> {
    pagination: {
      page: number;
      limit: number;
      total: number;
      totalPages: number;
    };
  }
  ```

## Component Architecture

### Component Types
- **UI Components**: Presentational, reusable components
- **Feature Components**: Business logic components
- **Layout Components**: Page structure and navigation
- **Page Components**: Route-level components

### Component Conventions
- Use PascalCase for component names
- Use descriptive, specific names
- Follow single responsibility principle
- Example structure:
  ```typescript
  // components/ui/Button.tsx
  import { ButtonHTMLAttributes, forwardRef } from 'react';
  import { cn } from '@/utils/cn';

  interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'destructive';
    size?: 'sm' | 'md' | 'lg';
    loading?: boolean;
  }

  export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant = 'primary', size = 'md', loading, children, ...props }, ref) => {
      return (
        <button
          ref={ref}
          className={cn(
            'btn',
            `btn-${variant}`,
            `btn-${size}`,
            { 'btn-loading': loading },
            className
          )}
          disabled={loading}
          {...props}
        >
          {loading ? 'Loading...' : children}
        </button>
      );
    }
  );
  ```

### Compound Components
- Use compound components for complex UI patterns
- Example:
  ```typescript
  // components/ui/Card.tsx
  interface CardProps {
    children: React.ReactNode;
    className?: string;
  }

  interface CardHeaderProps {
    children: React.ReactNode;
    className?: string;
  }

  interface CardContentProps {
    children: React.ReactNode;
    className?: string;
  }

  export const Card = ({ children, className }: CardProps) => (
    <div className={cn('card', className)}>
      {children}
    </div>
  );

  export const CardHeader = ({ children, className }: CardHeaderProps) => (
    <div className={cn('card-header', className)}>
      {children}
    </div>
  );

  export const CardContent = ({ children, className }: CardContentProps) => (
    <div className={cn('card-content', className)}>
      {children}
    </div>
  );

  Card.Header = CardHeader;
  Card.Content = CardContent;
  ```

## State Management

### Redux Toolkit (Recommended)
- Use Redux Toolkit for complex state management
- Organize store by feature slices
- Use RTK Query for API state management
- Example store setup:
  ```typescript
  // store/index.ts
  import { configureStore } from '@reduxjs/toolkit';
  import { authSlice } from './auth/authSlice';
  import { userSlice } from './user/userSlice';
  import { api } from './api';

  export const store = configureStore({
    reducer: {
      auth: authSlice.reducer,
      user: userSlice.reducer,
      api: api.reducer,
    },
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(api.middleware),
  });

  export type RootState = ReturnType<typeof store.getState>;
  export type AppDispatch = typeof store.dispatch;
  ```

### Slice Pattern
- Create feature-specific slices
- Use createSlice for consistent structure
- Example:
  ```typescript
  // store/auth/authSlice.ts
  import { createSlice, PayloadAction } from '@reduxjs/toolkit';
  import { User } from '@/types/user';

  interface AuthState {
    user: User | null;
    token: string | null;
    isLoading: boolean;
    error: string | null;
  }

  const initialState: AuthState = {
    user: null,
    token: localStorage.getItem('token'),
    isLoading: false,
    error: null,
  };

  export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
      loginStart: (state) => {
        state.isLoading = true;
        state.error = null;
      },
      loginSuccess: (state, action: PayloadAction<{ user: User; token: string }>) => {
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.isLoading = false;
        state.error = null;
      },
      loginFailure: (state, action: PayloadAction<string>) => {
        state.isLoading = false;
        state.error = action.payload;
      },
      logout: (state) => {
        state.user = null;
        state.token = null;
        state.error = null;
      },
    },
  });

  export const { loginStart, loginSuccess, loginFailure, logout } = authSlice.actions;
  ```

### RTK Query API
- Use RTK Query for API calls
- Define API endpoints with proper typing
- Example:
  ```typescript
  // store/api/userApi.ts
  import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
  import { User, CreateUserRequest, PaginatedResponse } from '@/types/user';

  export const userApi = createApi({
    reducerPath: 'userApi',
    baseQuery: fetchBaseQuery({
      baseUrl: '/api/v1/users',
      prepareHeaders: (headers, { getState }) => {
        const token = (getState() as RootState).auth.token;
        if (token) {
          headers.set('authorization', `Bearer ${token}`);
        }
        return headers;
      },
    }),
    tagTypes: ['User'],
    endpoints: (builder) => ({
      getUsers: builder.query<PaginatedResponse<User>, { page?: number; limit?: number }>({
        query: ({ page = 1, limit = 20 }) => `?page=${page}&limit=${limit}`,
        providesTags: ['User'],
      }),
      getUser: builder.query<User, string>({
        query: (id) => `/${id}`,
        providesTags: (result, error, id) => [{ type: 'User', id }],
      }),
      createUser: builder.mutation<User, CreateUserRequest>({
        query: (user) => ({
          url: '',
          method: 'POST',
          body: user,
        }),
        invalidatesTags: ['User'],
      }),
      updateUser: builder.mutation<User, { id: string; user: Partial<User> }>({
        query: ({ id, user }) => ({
          url: `/${id}`,
          method: 'PUT',
          body: user,
        }),
        invalidatesTags: (result, error, { id }) => [{ type: 'User', id }],
      }),
      deleteUser: builder.mutation<void, string>({
        query: (id) => ({
          url: `/${id}`,
          method: 'DELETE',
        }),
        invalidatesTags: ['User'],
      }),
    }),
  });

  export const {
    useGetUsersQuery,
    useGetUserQuery,
    useCreateUserMutation,
    useUpdateUserMutation,
    useDeleteUserMutation,
  } = userApi;
  ```

## Custom Hooks

### Hook Conventions
- Start hook names with "use"
- Keep hooks focused and reusable
- Use TypeScript for hook parameters and return types
- Example hooks:

### useLocalStorage Hook
```typescript
// hooks/useLocalStorage.ts
import { useState, useEffect } from 'react';

export function useLocalStorage<T>(
  key: string,
  initialValue: T
): [T, (value: T | ((val: T) => T)) => void] {
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error);
      return initialValue;
    }
  });

  const setValue = (value: T | ((val: T) => T)) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error);
    }
  };

  return [storedValue, setValue];
}
```

### useDebounce Hook
```typescript
// hooks/useDebounce.ts
import { useState, useEffect } from 'react';

export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}
```

### useApi Hook
```typescript
// hooks/useApi.ts
import { useState, useEffect } from 'react';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export function useApi<T>(
  apiCall: () => Promise<T>,
  dependencies: unknown[] = []
): UseApiState<T> & { refetch: () => void } {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: true,
    error: null,
  });

  const fetchData = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    try {
      const data = await apiCall();
      setState({ data, loading: false, error: null });
    } catch (error) {
      setState({
        data: null,
        loading: false,
        error: error instanceof Error ? error.message : 'An error occurred',
      });
    }
  };

  useEffect(() => {
    fetchData();
  }, dependencies);

  return { ...state, refetch: fetchData };
}
```

## Styling & UI

### CSS-in-JS with Styled Components (Alternative)
```typescript
// styles/theme.ts
export const theme = {
  colors: {
    primary: '#0066cc',
    secondary: '#6c757d',
    success: '#28a745',
    danger: '#dc3545',
    warning: '#ffc107',
    info: '#17a2b8',
    light: '#f8f9fa',
    dark: '#343a40',
  },
  spacing: {
    xs: '0.25rem',
    sm: '0.5rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '3rem',
  },
  breakpoints: {
    sm: '576px',
    md: '768px',
    lg: '992px',
    xl: '1200px',
  },
};
```

### Tailwind CSS (Recommended)
- Use Tailwind for utility-first styling
- Create custom components for repeated patterns
- Use clsx or cn utility for conditional classes
- Example utility:
  ```typescript
  // utils/cn.ts
  import { clsx, type ClassValue } from 'clsx';
  import { twMerge } from 'tailwind-merge';

  export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
  }
  ```

### Component Styling
```typescript
// components/ui/Alert.tsx
import { cn } from '@/utils/cn';

interface AlertProps {
  variant?: 'default' | 'success' | 'warning' | 'error';
  children: React.ReactNode;
  className?: string;
}

const alertVariants = {
  default: 'bg-blue-50 border-blue-200 text-blue-800',
  success: 'bg-green-50 border-green-200 text-green-800',
  warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
  error: 'bg-red-50 border-red-200 text-red-800',
};

export const Alert = ({ variant = 'default', children, className }: AlertProps) => {
  return (
    <div
      className={cn(
        'border rounded-lg p-4',
        alertVariants[variant],
        className
      )}
    >
      {children}
    </div>
  );
};
```

## Form Handling

### React Hook Form
- Use React Hook Form for form management
- Integrate with Zod for validation
- Example form setup:
  ```typescript
  // components/forms/UserForm.tsx
  import { useForm } from 'react-hook-form';
  import { zodResolver } from '@hookform/resolvers/zod';
  import { z } from 'zod';

  const userSchema = z.object({
    username: z.string().min(3, 'Username must be at least 3 characters'),
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    role: z.enum(['admin', 'user', 'viewer']),
  });

  type UserFormData = z.infer<typeof userSchema>;

  interface UserFormProps {
    onSubmit: (data: UserFormData) => void;
    defaultValues?: Partial<UserFormData>;
    isLoading?: boolean;
  }

  export const UserForm = ({ onSubmit, defaultValues, isLoading }: UserFormProps) => {
    const {
      register,
      handleSubmit,
      formState: { errors },
      reset,
    } = useForm<UserFormData>({
      resolver: zodResolver(userSchema),
      defaultValues,
    });

    const handleFormSubmit = (data: UserFormData) => {
      onSubmit(data);
      reset();
    };

    return (
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
        <div>
          <label htmlFor="username" className="block text-sm font-medium">
            Username
          </label>
          <input
            {...register('username')}
            id="username"
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
          {errors.username && (
            <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium">
            Email
          </label>
          <input
            {...register('email')}
            id="email"
            type="email"
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
          {errors.email && (
            <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {isLoading ? 'Submitting...' : 'Submit'}
        </button>
      </form>
    );
  };
  ```

## Routing

### React Router v6
- Use React Router for client-side routing
- Implement protected routes
- Use lazy loading for code splitting
- Example routing setup:
  ```typescript
  // App.tsx
  import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
  import { lazy, Suspense } from 'react';
  import { useSelector } from 'react-redux';
  import { RootState } from '@/store';
  import { Layout } from '@/components/layout/Layout';
  import { LoadingSpinner } from '@/components/ui/LoadingSpinner';

  // Lazy load pages
  const Dashboard = lazy(() => import('@/features/dashboard/pages/DashboardPage'));
  const Users = lazy(() => import('@/features/users/pages/UsersPage'));
  const Login = lazy(() => import('@/features/auth/pages/LoginPage'));

  const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
    const isAuthenticated = useSelector((state: RootState) => !!state.auth.token);
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
  };

  export const App = () => {
    return (
      <BrowserRouter>
        <Suspense fallback={<LoadingSpinner />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/*"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Routes>
                      <Route path="/" element={<Navigate to="/dashboard" replace />} />
                      <Route path="/dashboard" element={<Dashboard />} />
                      <Route path="/users" element={<Users />} />
                    </Routes>
                  </Layout>
                </ProtectedRoute>
              }
            />
          </Routes>
        </Suspense>
      </BrowserRouter>
    );
  };
  ```

## Error Handling

### Error Boundaries
- Implement error boundaries for graceful error handling
- Example error boundary:
  ```typescript
  // components/ErrorBoundary.tsx
  import { Component, ErrorInfo, ReactNode } from 'react';

  interface ErrorBoundaryProps {
    children: ReactNode;
    fallback?: ReactNode;
  }

  interface ErrorBoundaryState {
    hasError: boolean;
    error?: Error;
  }

  export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    constructor(props: ErrorBoundaryProps) {
      super(props);
      this.state = { hasError: false };
    }

    static getDerivedStateFromError(error: Error): ErrorBoundaryState {
      return { hasError: true, error };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
      console.error('Error boundary caught an error:', error, errorInfo);
      // Log to error reporting service
    }

    render() {
      if (this.state.hasError) {
        return (
          this.props.fallback || (
            <div className="flex min-h-screen items-center justify-center">
              <div className="text-center">
                <h2 className="text-2xl font-bold text-gray-900">Something went wrong</h2>
                <p className="mt-2 text-gray-600">
                  We're sorry, but something unexpected happened.
                </p>
                <button
                  onClick={() => window.location.reload()}
                  className="mt-4 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                >
                  Reload Page
                </button>
              </div>
            </div>
          )
        );
      }

      return this.props.children;
    }
  }
  ```

### API Error Handling
```typescript
// utils/errorHandler.ts
export interface ApiError {
  message: string;
  status?: number;
  code?: string;
}

export const handleApiError = (error: unknown): ApiError => {
  if (error instanceof Error) {
    return {
      message: error.message,
      status: (error as any).status,
      code: (error as any).code,
    };
  }
  
  return {
    message: 'An unexpected error occurred',
  };
};

export const showErrorToast = (error: ApiError) => {
  // Integration with toast library
  console.error('API Error:', error);
};
```

## Testing

### Testing Setup
- Use Jest and React Testing Library
- Write tests for components, hooks, and utilities
- Example test structure:
  ```typescript
  // components/ui/__tests__/Button.test.tsx
  import { render, screen, fireEvent } from '@testing-library/react';
  import { Button } from '../Button';

  describe('Button', () => {
    it('renders children correctly', () => {
      render(<Button>Click me</Button>);
      expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
    });

    it('handles click events', () => {
      const handleClick = jest.fn();
      render(<Button onClick={handleClick}>Click me</Button>);
      
      fireEvent.click(screen.getByRole('button'));
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('shows loading state', () => {
      render(<Button loading>Click me</Button>);
      
      expect(screen.getByRole('button')).toBeDisabled();
      expect(screen.getByText(/loading/i)).toBeInTheDocument();
    });

    it('applies variant classes correctly', () => {
      render(<Button variant="destructive">Delete</Button>);
      
      expect(screen.getByRole('button')).toHaveClass('btn-destructive');
    });
  });
  ```

### Hook Testing
```typescript
// hooks/__tests__/useLocalStorage.test.ts
import { renderHook, act } from '@testing-library/react';
import { useLocalStorage } from '../useLocalStorage';

describe('useLocalStorage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('returns initial value when localStorage is empty', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'initial'));
    
    expect(result.current[0]).toBe('initial');
  });

  it('updates localStorage when value changes', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'initial'));
    
    act(() => {
      result.current[1]('updated');
    });
    
    expect(result.current[0]).toBe('updated');
    expect(localStorage.getItem('test-key')).toBe('"updated"');
  });
});
```

## Performance Optimization

### Code Splitting
- Use lazy loading for routes and heavy components
- Implement React.memo for expensive components
- Example:
  ```typescript
  // components/ExpensiveComponent.tsx
  import { memo } from 'react';

  interface ExpensiveComponentProps {
    data: ComplexData[];
    onProcess: (item: ComplexData) => void;
  }

  export const ExpensiveComponent = memo<ExpensiveComponentProps>(
    ({ data, onProcess }) => {
      // Expensive rendering logic
      return (
        <div>
          {data.map((item) => (
            <ExpensiveItem key={item.id} item={item} onProcess={onProcess} />
          ))}
        </div>
      );
    },
    (prevProps, nextProps) => {
      // Custom comparison logic
      return prevProps.data.length === nextProps.data.length;
    }
  );
  ```

### useMemo and useCallback
```typescript
// Example of proper memoization
const SearchableUserList = ({ users, searchTerm }: Props) => {
  const filteredUsers = useMemo(() => {
    return users.filter(user => 
      user.username.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [users, searchTerm]);

  const handleUserSelect = useCallback((userId: string) => {
    // Handle user selection
  }, []);

  return (
    <div>
      {filteredUsers.map(user => (
        <UserCard 
          key={user.id} 
          user={user} 
          onSelect={handleUserSelect}
        />
      ))}
    </div>
  );
};
```

## Build Configuration

### Vite Configuration
```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    target: 'esnext',
    minify: 'esbuild',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
          ui: ['@headlessui/react', '@heroicons/react'],
        },
      },
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
```

### Environment Configuration
```typescript
// lib/env.ts
import { z } from 'zod';

const envSchema = z.object({
  VITE_API_BASE_URL: z.string().url(),
  VITE_APP_TITLE: z.string().default('Telepesa Dashboard'),
  VITE_ENABLE_ANALYTICS: z
    .string()
    .transform((val) => val === 'true')
    .default('false'),
});

export const env = envSchema.parse(import.meta.env);
```

## Accessibility & User Experience

### Accessibility (A11y)
- Use semantic HTML elements
- Implement proper ARIA attributes
- Ensure keyboard navigation works
- Test with screen readers
- Maintain color contrast ratios
- Example:
  ```typescript
  // components/ui/Modal.tsx
  import { useEffect, useRef } from 'react';

  interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
  }

  export const Modal = ({ isOpen, onClose, title, children }: ModalProps) => {
    const modalRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
      if (isOpen) {
        modalRef.current?.focus();
      }
    }, [isOpen]);

    if (!isOpen) return null;

    return (
      <div
        className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
        onClick={onClose}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        <div
          ref={modalRef}
          className="bg-white rounded-lg p-6 max-w-md w-full mx-4"
          onClick={(e) => e.stopPropagation()}
          tabIndex={-1}
        >
          <h2 id="modal-title" className="text-xl font-semibold mb-4">
            {title}
          </h2>
          {children}
        </div>
      </div>
    );
  };
  ```

### Internationalization (i18n)
- Use react-i18next for multi-language support
- Implement locale switching
- Handle RTL languages
- Format dates, numbers, and currencies properly
- Example:
  ```typescript
  // hooks/useTranslation.ts
  import { useTranslation as useI18nTranslation } from 'react-i18next';

  export const useTranslation = (namespace?: string) => {
    const { t, i18n } = useI18nTranslation(namespace);

    const changeLanguage = (language: string) => {
      i18n.changeLanguage(language);
    };

    return { t, changeLanguage, currentLanguage: i18n.language };
  };
  ```

### Dark Mode Support
- Implement system preference detection
- Provide manual theme switching
- Persist theme preferences
- Use CSS variables for theme colors

## Real-time Features

### WebSocket Integration
- Use Socket.IO or native WebSocket for real-time updates
- Implement connection state management
- Handle reconnection logic
- Example:
  ```typescript
  // hooks/useWebSocket.ts
  import { useEffect, useState, useRef } from 'react';

  interface UseWebSocketOptions {
    onMessage?: (data: any) => void;
    onConnect?: () => void;
    onDisconnect?: () => void;
  }

  export const useWebSocket = (url: string, options: UseWebSocketOptions = {}) => {
    const [isConnected, setIsConnected] = useState(false);
    const [lastMessage, setLastMessage] = useState<any>(null);
    const ws = useRef<WebSocket | null>(null);

    useEffect(() => {
      ws.current = new WebSocket(url);

      ws.current.onopen = () => {
        setIsConnected(true);
        options.onConnect?.();
      };

      ws.current.onmessage = (event) => {
        const data = JSON.parse(event.data);
        setLastMessage(data);
        options.onMessage?.(data);
      };

      ws.current.onclose = () => {
        setIsConnected(false);
        options.onDisconnect?.();
      };

      return () => {
        ws.current?.close();
      };
    }, [url]);

    const sendMessage = (message: any) => {
      if (ws.current?.readyState === WebSocket.OPEN) {
        ws.current.send(JSON.stringify(message));
      }
    };

    return { isConnected, lastMessage, sendMessage };
  };
  ```

## Progressive Web App (PWA)

### Service Worker
- Implement service worker for offline support
- Cache critical resources
- Handle background sync
- Show offline indicators

### App Manifest
- Configure web app manifest
- Support installation prompts
- Handle different screen sizes

## Security

### Content Security Policy (CSP)
- Implement strict CSP headers
- Prevent XSS attacks
- Validate all user inputs
- Sanitize HTML content

### CSRF Protection
- Implement CSRF tokens
- Use SameSite cookies
- Validate request origins

## Performance Monitoring

### Analytics Integration
- Integrate Google Analytics or similar
- Track user interactions
- Monitor performance metrics
- Implement custom event tracking

### Error Monitoring
- Use Sentry or similar for error tracking
- Implement user feedback collection
- Track JavaScript errors and performance issues

## Offline Support

### Caching Strategies
- Implement service worker caching
- Use IndexedDB for offline data
- Show offline/online status
- Sync data when connection restored

## Payment Integration

### Stripe Integration
- Implement secure payment flows
- Handle payment methods properly
- Manage subscription billing
- Handle webhooks for payment events
- Example:
  ```typescript
  // hooks/useStripe.ts
  import { loadStripe } from '@stripe/stripe-js';
  import { useState } from 'react';

  const stripePromise = loadStripe(process.env.VITE_STRIPE_PUBLIC_KEY!);

  export const useStripePayment = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const processPayment = async (paymentMethodId: string, amount: number) => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch('/api/payments/process', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ paymentMethodId, amount }),
        });

        const result = await response.json();
        return result;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Payment failed');
        throw err;
      } finally {
        setLoading(false);
      }
    };

    return { processPayment, loading, error };
  };
  ```

## Data Visualization

### Chart Libraries
- Use Recharts or Chart.js for data visualization
- Implement responsive charts
- Handle large datasets efficiently
- Provide accessibility for charts

## File Upload & Management

### File Upload
- Implement drag-and-drop file upload
- Show upload progress
- Handle file validation
- Support multiple file types
- Example:
  ```typescript
  // hooks/useFileUpload.ts
  import { useState, useCallback } from 'react';

  export const useFileUpload = () => {
    const [uploading, setUploading] = useState(false);
    const [progress, setProgress] = useState(0);

    const uploadFile = useCallback(async (file: File, onProgress?: (progress: number) => void) => {
      setUploading(true);
      setProgress(0);

      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData,
        });

        if (!response.ok) throw new Error('Upload failed');

        return await response.json();
      } finally {
        setUploading(false);
        setProgress(0);
      }
    }, []);

    return { uploadFile, uploading, progress };
  };
  ```

This comprehensive React rules file provides guidelines for building a modern, scalable dashboard application that integrates well with your Spring Boot backend and follows the same quality standards as your Android application. 