// Rota dinâmica para perfil de outros usuários
"use client";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import axiosInstance from "@/lib/api/axiosInstance";
import { Button } from "@/components/ui/Button";
import { useToast } from "@/components/messages/ToastProvider";

export default function UserProfilePage() {
  const { userId } = useParams();
  const { toast } = useToast();

  const { data: user, isLoading } = useQuery({
    queryKey: ["user", userId],
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/users/${userId}`);
      return res.data;
    },
  });

  const handleAddFriend = async () => {
    try {
      await axiosInstance.post(`/api/friends`, { friendId: userId });
      toast({ title: "Success", description: "Friend request sent" });
    } catch (error) {
      toast({ title: "Error", description: "Failed to send friend request", variant: "destructive" });
    }
  };

  if (isLoading) return <div>Loading...</div>;
  if (!user) return <div>User not found</div>;

  return (
    <div>
      <h1>{user.name}&apos;s Profile</h1>
      <p>Email: {user.email}</p>
      <Button onClick={handleAddFriend}>Add Friend</Button>
    </div>
  );
}