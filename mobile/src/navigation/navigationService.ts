import {createNavigationContainerRef} from '@react-navigation/native';
import type {RootStackParamList} from './routes';

export const navigationRef = createNavigationContainerRef<RootStackParamList>();

export function resetToLogin() {
  if (navigationRef.isReady()) {
    navigationRef.reset({
      index: 0,
      routes: [{name: 'Login'}],
    });
  }
}
