<template>
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path v-if="name === 'home'" d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z" />
    <path v-else-if="name === 'ledger'" d="M7 3.5h8.5L19 7v13.5H7zM15 3.8V7h3.2M9 12h6M9 16h6" />
    <path v-else-if="name === 'habit'" d="M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM8.5 12.2l2.4 2.4 4.6-5" />
    <path v-else-if="name === 'fitness'" d="M3 12h3l2-5 4 10 2-5h7" />
    <path v-else-if="name === 'schedule'" d="M6 5.5h12A1.5 1.5 0 0 1 19.5 7v11A1.5 1.5 0 0 1 18 19.5H6A1.5 1.5 0 0 1 4.5 18V7A1.5 1.5 0 0 1 6 5.5zM8 3.5v4M16 3.5v4M4.5 9.5h15" />
    <path v-else-if="name === 'buy'" d="M5 7h15l-1.4 8.2a1.5 1.5 0 0 1-1.5 1.3H8.2A1.5 1.5 0 0 1 6.7 15L5 7zm0 0L4.2 4H2.5M9 20.2h.1M16 20.2h.1" />
    <path v-else-if="name === 'media'" d="M5 5h6v6H5zM13 5h6v6h-6zM5 13h6v6H5zM13 13h6v6h-6z" />
    <path v-else-if="name === 'settings'" d="M12 15.2a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4zM12 3.5v2.2M12 18.3v2.2M4.8 6.2l1.6 1.6M17.6 16.2l1.6 1.6M3.5 12h2.2M18.3 12h2.2M4.8 17.8l1.6-1.6M17.6 7.8l1.6-1.6" />
    <path v-else-if="name === 'exit'" d="M10 5.5H6.5A1.5 1.5 0 0 0 5 7v10a1.5 1.5 0 0 0 1.5 1.5H10M14 8.5 17.5 12 14 15.5M9.5 12h8" />
    <path v-else d="M6 5.5h12A1.5 1.5 0 0 1 19.5 7v11A1.5 1.5 0 0 1 18 19.5H6A1.5 1.5 0 0 1 4.5 18V7A1.5 1.5 0 0 1 6 5.5zM8 3.5v4M16 3.5v4M4.5 9.5h15M8 13h4M8 16h6" />
  </svg>
</template>

<script setup lang="ts">
defineProps<{ name: string }>();
</script>
