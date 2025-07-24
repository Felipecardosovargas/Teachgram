"use client";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import axiosInstance from "@/lib/api/axiosInstance";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { useToast } from "@/components/messages/ToastProvider";
import type { AxiosError } from "axios";
import type { ApiError } from "next/dist/server/api-utils";

const postSchema = z.object({
  title: z.string().min(1, "Title is required").max(50, "Title must be under 50 characters"),
  description: z.string().max(200, "Description must be under 200 characters").optional(),
  photoLink: z.string().url("Invalid URL").optional(),
  videoLink: z.string().url("Invalid URL").optional(),
  private: z.boolean(),
});

type PostForm = z.infer<typeof postSchema>;

export default function CreatePostForm() {
  const { toast } = useToast();
  const { register, handleSubmit, formState: { errors } } = useForm<PostForm>({
    resolver: zodResolver(postSchema),
  });

  const onSubmit = async (data: PostForm) => {
    try {
      await axiosInstance.post("/api/posts", data);
      toast({ title: "Success", description: "Post created" });
    } catch (error) {
      const axiosError = error as AxiosError<ApiError>;
  
      const description =
        axiosError.response?.data?.message ||
        "Failed to create post";
  
      toast({
        title: "Error",
        description,
        variant: "destructive",
      });
    }
  };
  
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input {...register("title")} placeholder="Title" />
      {errors.title && <p className="text-red-500">{errors.title.message}</p>}
      <Input {...register("description")} placeholder="Description (optional)" />
      {errors.description && <p className="text-red-500">{errors.description.message}</p>}
      <Input {...register("photoLink")} placeholder="Photo URL (optional)" />
      {errors.photoLink && <p className="text-red-500">{errors.photoLink.message}</p>}
      <Input {...register("videoLink")} placeholder="Video URL (optional)" />
      {errors.videoLink && <p className="text-red-500">{errors.videoLink.message}</p>}
      <label>
        <input type="checkbox" {...register("private")} /> Private
      </label>
      <Button type="submit">Create Post</Button>
    </form>
  );
}