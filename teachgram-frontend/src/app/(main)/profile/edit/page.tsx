// src/app/(main)/profile/edit/page.tsx
"use client";

import React, { useState } from "react"; // Importar useState para gerenciar estados de UI
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuthStore } from "@/context/AuthContext";
import axiosInstance from "@/lib/api/axiosInstance";
import { Button } from "@/components/ui/Button"; // Capitalização corrigida
import { Input } from "@/components/ui/Input";   // Capitalização corrigida
import { useToast } from "@/components/messages/ToastProvider";
import { Modal } from "@/components/ui/Modal"; // Assumindo que você tem um componente Modal

// Esquema de validação para o formulário de edição de perfil
const editProfileSchema = z.object({
  name: z.string().min(2, "O nome deve ter pelo menos 2 caracteres."),
  email: z.string().email("Email inválido."),
  phone: z.string().optional(), // Telefone é opcional
  bio: z.string().max(200, "A biografia deve ter no máximo 200 caracteres.").optional(), // Bio é opcional
  profilePicture: z.string().url("URL de imagem de perfil inválida.").optional(), // URL da imagem é opcional
});

// Tipo inferido do esquema Zod para o formulário
type EditProfileForm = z.infer<typeof editProfileSchema>;

export default function EditProfilePage() {
  // Obtém o usuário e a função setUser do store de autenticação
  const { user, setUser } = useAuthStore();
  // Obtém a função toast do provedor de toasts
  const { toast } = useToast();

  // Estados para gerenciar o carregamento e a exibição do modal de exclusão
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // Configuração do react-hook-form
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset, // Adicionado reset para atualizar os valores padrão após a edição
  } = useForm<EditProfileForm>({
    resolver: zodResolver(editProfileSchema),
    // Define os valores padrão do formulário com base nos dados do usuário.
    // Usamos o operador '??' para garantir que, se user ou uma propriedade for null/undefined,
    // um valor padrão (string vazia) seja usado, evitando problemas de tipo.
    defaultValues: {
      name: user?.name ?? '',
      email: user?.email ?? '',
      phone: user?.phone ?? '',
      bio: user?.bio ?? '',
      profilePicture: user?.profilePicture ?? '',
    },
  });

  // Efeito para atualizar os valores padrão do formulário quando o objeto 'user' muda
  // Isso é importante caso o 'user' seja carregado assincronamente ou atualizado em outro lugar
  React.useEffect(() => {
    if (user) {
      reset({
        name: user.name ?? '',
        email: user.email ?? '',
        phone: user.phone ?? '',
        bio: user.bio ?? '',
        profilePicture: user.profilePicture ?? '',
      });
    }
  }, [user, reset]); // Dependências: user e reset (que é estável)

  // Função para lidar com a submissão do formulário de edição
  const onSubmit = async (data: EditProfileForm) => {
    if (!user?.id) {
      toast({ title: "Erro", description: "ID do usuário não encontrado para atualização.", variant: "destructive" });
      return;
    }

    setIsSubmitting(true); // Inicia o estado de submissão
    try {
      const res = await axiosInstance.put(`/api/users/${user.id}`, data);

      // Garante que o objeto passado para setUser tenha todas as propriedades obrigatórias
      // com valores string, usando fallbacks para evitar erros de tipo.
      const updatedUser = {
        id: res.data.id || user.id || '', // Usa o ID da resposta, ou o ID existente, ou vazio
        name: res.data.name || data.name || '', // Usa o nome da resposta, ou do formulário, ou vazio
        email: res.data.email || data.email || '', // Usa o email da resposta, ou do formulário, ou vazio
        token: user.token || '', // Mantém o token existente
        phone: res.data.phone || data.phone || '', // Adicionado phone
        bio: res.data.bio || data.bio || '', // Adicionado bio
        profilePicture: res.data.profilePicture || data.profilePicture || '', // Adicionado profilePicture
      };

      setUser(updatedUser); // Atualiza o usuário no store
      toast({ title: "Sucesso", description: "Perfil atualizado com sucesso!", variant: "success" });
    } catch (_error) { // Renomeado 'error' para '_error' para ESLint
      toast({ title: "Erro", description: "Falha ao atualizar o perfil.", variant: "destructive" });
    } finally {
      setIsSubmitting(false); // Finaliza o estado de submissão
    }
  };

  // Função para lidar com a exclusão da conta
  const handleDeleteConfirm = async () => {
    if (!user?.id) {
      toast({ title: "Erro", description: "ID do usuário não encontrado para exclusão.", variant: "destructive" });
      setShowDeleteModal(false);
      return;
    }

    setIsDeleting(true); // Inicia o estado de exclusão
    setShowDeleteModal(false); // Fecha o modal após confirmar
    try {
      await axiosInstance.delete(`/api/users/${user.id}`);
      toast({ title: "Sucesso", description: "Conta excluída com sucesso!", variant: "success" });
      // Redireciona para a página de login após a exclusão bem-sucedida
      useAuthStore.getState().clearUser(); // Limpa o usuário do store
      window.location.href = "/login";
    } catch (_error) { // Renomeado 'error' para '_error' para ESLint
      toast({ title: "Erro", description: "Falha ao excluir a conta.", variant: "destructive" });
    } finally {
      setIsDeleting(false); // Finaliza o estado de exclusão
    }
  };

  // Se o usuário não estiver carregado ou não existir, exibe uma mensagem ou redireciona
  if (!user) {
    // Poderia redirecionar para a página de login aqui se o usuário não estiver logado
    return <div className="p-4 text-center text-gray-600">Carregando perfil ou usuário não logado...</div>;
  }

  return (
    <div className="p-4 max-w-lg mx-auto bg-white rounded-xl shadow-md space-y-6">
      <h1 className="text-3xl font-bold text-center text-gray-900 mb-6">Editar Perfil</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Nome</label>
          <Input id="name" {...register("name")} placeholder="Seu nome" disabled={isSubmitting} />
          {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name.message}</p>}
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <Input id="email" type="email" {...register("email")} placeholder="Seu email" disabled={isSubmitting} />
          {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Telefone (opcional)</label>
          <Input id="phone" {...register("phone")} placeholder="Seu telefone" disabled={isSubmitting} />
        </div>

        <div>
          <label htmlFor="bio" className="block text-sm font-medium text-gray-700 mb-1">Biografia (opcional)</label>
          <Input id="bio" {...register("bio")} placeholder="Fale um pouco sobre você" disabled={isSubmitting} />
          {errors.bio && <p className="text-red-500 text-sm mt-1">{errors.bio.message}</p>}
        </div>

        <div>
          <label htmlFor="profilePicture" className="block text-sm font-medium text-gray-700 mb-1">URL da Imagem de Perfil (opcional)</label>
          <Input id="profilePicture" {...register("profilePicture")} placeholder="https://example.com/sua-foto.jpg" disabled={isSubmitting} />
          {errors.profilePicture && <p className="text-red-500 text-sm mt-1">{errors.profilePicture.message}</p>}
        </div>

        <Button
          type="submit"
          className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition duration-200"
          disabled={isSubmitting}
        >
          {isSubmitting ? "Salvando..." : "Salvar Alterações"}
        </Button>
      </form>

      <div className="border-t pt-4 mt-6">
        <Button
          variant="destructive"
          onClick={() => setShowDeleteModal(true)} // Abre o modal de confirmação
          className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition duration-200"
          disabled={isDeleting}
        >
          {isDeleting ? "Excluindo..." : "Excluir Conta"}
        </Button>
      </div>

      {/* Modal de Confirmação de Exclusão */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="Confirmar Exclusão de Conta"
      >
        <p className="text-gray-700 mb-4">Tem certeza de que deseja excluir sua conta? Esta ação é irreversível.</p>
        <div className="flex justify-end space-x-3">
          <Button
            variant="default"
            onClick={() => setShowDeleteModal(false)}
            disabled={isDeleting}
          >
            Cancelar
          </Button>
          <Button
            variant="destructive"
            onClick={handleDeleteConfirm}
            disabled={isDeleting}
          >
            {isDeleting ? "Excluindo..." : "Confirmar Exclusão"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
