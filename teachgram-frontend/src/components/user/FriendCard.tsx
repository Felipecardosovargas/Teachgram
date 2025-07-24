import Link from "next/link";
import { Card } from "@/components/ui";

type Friend = {
  id: string;
  name: string;
  profilePicture?: string;
};

export function FriendCard({ friend }: { friend: Friend }) {
  return (
    <Link href={`/profile/${friend.id}`}>
      <Card className="p-4 flex items-center space-x-2">
        <img
          src={friend.profilePicture || "/images/default-avatar.png"}
          alt="Profile"
          className="w-10 h-10 rounded-full"
        />
        <p>{friend.name}</p>
      </Card>
    </Link>
  );
}