import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';

// https://astro.build/config
export default defineConfig({
  site: 'https://jayakrishnanps.github.io',
  base: '/Ascent',
  integrations: [tailwind()]
});
