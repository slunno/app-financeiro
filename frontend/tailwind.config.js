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
        dark: {
          bg: '#0F172A',
          card: '#1E293B',
          border: '#334155',
          hover: '#334155',
        },
        brand: {
          green: '#10B981',
          'green-dark': '#059669',
          red: '#EF4444',
          gold: '#F59E0B',
          'gold-bright': '#FBBF24',
          accent: '#3B82F6',
        }
      }
    },
  },
  plugins: [],
}
