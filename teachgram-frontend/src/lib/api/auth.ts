import axios from "axios";
import axiosInstance from "./axiosInstance";

export type LoginPayload = {
  username: string;
  password: string;
};

export type CreateUserPayload = {
  name: string;
  username: string;
  email: string;
  phone: string;
  password: string;
  profileLink: string;
  description: string;
};

type APIError = {
  message: string;
  status?: number;
  data?: Record<string, unknown>;
  originalError?: unknown;
};

export const loginUser = async (payload: LoginPayload) => {
  try {
    const response = await axiosInstance.post("/auth/signin", payload);
    return response.data;
  } catch (error: unknown) {
    throw handleAPIError(error, "Erro ao fazer login");
  }
};

export const createUser = async (payload: CreateUserPayload) => {
  try {
    const response = await axiosInstance.post("/auth/signup", payload);
    return response.data;
  } catch (error: unknown) {
    throw handleAPIError(error, "Erro ao criar usuário");
  }
};

function handleAPIError(error: unknown, defaultMessage = "Erro inesperado"): APIError {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      console.error(`[API ERROR] ${defaultMessage}:`, error.response.data);
      return {
        message: error.response.data?.message || defaultMessage,
        status: error.response.status,
        data: error.response.data,
        originalError: error,
      };
    } else if (error.request) {
      console.error(`[API ERROR] Nenhuma resposta recebida:`, error.request);
      return {
        message: "Nenhuma resposta recebida do servidor.",
        originalError: error,
      };
    } else {
      console.error(`[API ERROR] Erro de configuração:`, error.message);
      return {
        message: error.message || defaultMessage,
        originalError: error,
      };
    }
  } else {
    console.error(`[API ERROR] Erro inesperado:`, error);
    return {
      message: defaultMessage,
      originalError: error,
    };
  }
}
