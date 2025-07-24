"use client";
import { useState } from "react";
import { motion } from "framer-motion";
import { formatDate } from "@/lib/utils/formatDate";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import Image from "next/image";

export type Post = {
  private: unknown;
  id: string;
  title: string;
  description: string;
  photoLink?: string;
  videoLink?: string;
  user: { name: string; profilePicture?: string };
  createdAt: string;
  likes: number;
};

export function PostCard({ post }: { post: Post }) {
  const [likes, setLikes] = useState(post.likes);

  const handleLike = () => {
    setLikes(likes + 1);
  };

  return (
    <Card className="p-4">
      <div className="flex items-center space-x-2">
          <Image
            src={post.user.profilePicture || "/images/default-avatar.png"}
            alt="Profile"
            width={40}
            height={40}
            className="w-10 h-10 rounded-full object-cover"
          />
        <div>
          <p>{post.user.name}</p>
          <p className="text-sm text-gray-500">{formatDate(post.createdAt)}</p>
        </div>
      </div>
      <h2 className="mt-2">{post.title}</h2>
      <p>{post.description}</p>
      {post.photoLink && (
        <Image
          src={post.photoLink}
          alt="Post"
          width={500} 
          height={300}
          className="mt-2 rounded-md object-cover w-full"
        />
      )}
      {post.videoLink && (
        <video controls className="mt-2">
          <source src={post.videoLink} />
        </video>
      )}
      <motion.div
        whileTap={{ scale: 1.2 }}
        className="mt-2"
      >
        <Button onClick={handleLike}>Like ({likes})</Button>
      </motion.div>
    </Card>
  );
}