import { createPinia } from 'pinia';
import { useAuthStore } from './auth';
import { useEventStore } from './event';

const pinia = createPinia();

export {
  pinia,
  useAuthStore,
  useEventStore
}; 