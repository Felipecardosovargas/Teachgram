import i18n from "i18next";
import { initReactI18next } from "react-i18next";

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: { welcome: "Welcome to Teachgram" } },
    pt: { translation: { welcome: "Bem-vindo ao Teachgram" } },
  },
  lng: "en",
  fallbackLng: "en",
});

export default i18n;