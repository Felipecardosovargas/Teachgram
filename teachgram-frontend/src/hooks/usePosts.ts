import { useQuery } from "@tanstack/react-query";
import axiosInstance from "@/lib/api/axiosInstance";

export function usePosts(userId?: string) {
  return useQuery({
    queryKey: ["posts", userId],
    queryFn: async () => {
      const endpoint = userId ? `/api/users/${userId}/posts` : "/api/posts";
      const res = await axiosInstance.get(endpoint);
      return res.data;
    },
  });
}