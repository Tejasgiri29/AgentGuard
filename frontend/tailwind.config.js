/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: '#0a0d14',
        surface: '#121722',
        surfaceBorder: '#1e2638',
        primary: '#3b82f6',
        primaryHover: '#2563eb',
        accent: '#10b981',
        danger: '#ef4444',
        warning: '#f59e0b'
      }
    },
  },
  plugins: [],
}
