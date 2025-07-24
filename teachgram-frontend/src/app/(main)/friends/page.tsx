"use client";
import { useQuery } from "@tanstack/react-query";
import axiosInstance from "@/lib/api/axiosInstance";
import { FriendCard } from "@/components/user/FriendCard";
import { Spinner } from "@/components/ui/Spinner";
import { useAuthStore } from "@/context/AuthContext";

export default function FriendsPage() {
  const { user } = useAuthStore();
  const { data: friends, isLoading } = useQuery({
    queryKey: ["friends", user?.id],
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/users/${user?.id}/friends`);
      return res.data;
    },
  });

  if (isLoading) return <Spinner />;
  if (!friends) return <div>No friends found</div>;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {friends.map((friend: { id: string; name: string; profilePicture?: string }) => (
        <FriendCard key={friend.id} friend={friend} />
      ))}
    </div>
  );
}