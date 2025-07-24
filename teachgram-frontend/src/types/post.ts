import type { User } from "./user";

export interface Post {
  id: string;
  title: string;
  description?: string;
  photoLink?: string;
  videoLink?: string;
  private: boolean;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
  likesCount: number; 
  user: User;
}

export interface CreatePostRequest {
  title: string;
  description?: string;
  photoLink?: string;
  videoLink?: string;
  private: boolean;
  userId: string; 
}

export interface PostResponse {
  id: string;
  title: string;
  description?: string;
  photoLink?: string;
  videoLink?: string;
  private: boolean;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
  likesCount: number;

  user: {
    id: string;
    userName: string;
    profileLink?: string;
  };
}