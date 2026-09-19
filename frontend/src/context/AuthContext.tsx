import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, UserXP, AuthResponse } from '../types';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  userXp: UserXP | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: AuthResponse) => void;
  logout: () => void;
  updateUserXp: (newXp: UserXP) => void;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [userXp, setUserXp] = useState<UserXP | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('@AppFinanceiro:user');
    const storedXp = localStorage.getItem('@AppFinanceiro:userXp');
    const token = localStorage.getItem('@AppFinanceiro:token');

    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
      if (storedXp) setUserXp(JSON.parse(storedXp));
      
      // Valida o token / busca me()
      api.get('/auth/me')
        .then((res) => {
          setUser(res.data);
          localStorage.setItem('@AppFinanceiro:user', JSON.stringify(res.data));
        })
        .catch(() => logout())
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = (data: AuthResponse) => {
    localStorage.setItem('@AppFinanceiro:token', data.accessToken);
    localStorage.setItem('@AppFinanceiro:refreshToken', data.refreshToken);
    localStorage.setItem('@AppFinanceiro:user', JSON.stringify(data.user));
    localStorage.setItem('@AppFinanceiro:userXp', JSON.stringify(data.userXp));
    setUser(data.user);
    setUserXp(data.userXp);
  };

  const logout = () => {
    localStorage.removeItem('@AppFinanceiro:token');
    localStorage.removeItem('@AppFinanceiro:refreshToken');
    localStorage.removeItem('@AppFinanceiro:user');
    localStorage.removeItem('@AppFinanceiro:userXp');
    setUser(null);
    setUserXp(null);
  };

  const updateUserXp = (newXp: UserXP) => {
    setUserXp(newXp);
    localStorage.setItem('@AppFinanceiro:userXp', JSON.stringify(newXp));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        userXp,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        updateUserXp,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
