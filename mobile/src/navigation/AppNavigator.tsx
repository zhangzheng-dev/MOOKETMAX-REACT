import React, {useEffect} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {ActivityIndicator, AppState, StyleSheet, View} from 'react-native';
import {apiClient} from '../api/client';
import {DEFAULT_CATEGORY} from '../config/env';
import {HomeScreen} from '../screens/HomeScreen';
import {LoginScreen} from '../screens/LoginScreen';
import {sessionStore} from '../store/sessionStore';
import {colors} from '../theme/colors';
import {navigationRef} from './navigationService';
import type {RootStackParamList} from './routes';

const Stack = createNativeStackNavigator<RootStackParamList>();
const getSearchScreen = () => require('../screens/SearchScreen').SearchScreen;
const getHomeCardsScreen = () => require('../screens/HomeCardsScreen').HomeCardsScreen;
const getMerchantScreen = () => require('../screens/MerchantScreen').MerchantScreen;
const getProductScreen = () => require('../screens/ProductScreen').ProductScreen;
const getCountryScreen = () => require('../screens/CountryScreen').CountryScreen;
const getFactoryScreen = () => require('../screens/FactoryScreen').FactoryScreen;
const getCountryProductScreen = () => require('../screens/CountryProductScreen').CountryProductScreen;
const getCountryFactoryProductScreen = () =>
  require('../screens/CountryFactoryProductScreen').CountryFactoryProductScreen;
const getSubstituteProductScreen = () => require('../screens/SubstituteProductScreen').SubstituteProductScreen;
const getDataComparisonScreen = () => require('../screens/DataComparisonScreen').DataComparisonScreen;
const getBrandScreen = () => require('../screens/BrandScreen').BrandScreen;
const getBrandProductScreen = () => require('../screens/BrandProductScreen').BrandProductScreen;
const getProfileScreen = () => require('../screens/ProfileScreen').ProfileScreen;
const getEditProfileScreen = () => require('../screens/EditProfileScreen').EditProfileScreen;
const getInventoryScreen = () => require('../screens/InventoryScreen').default;

export function AppNavigator() {
  const {hydrate, isHydrated, token} = sessionStore();

  useEffect(() => {
    hydrate();
  }, [hydrate]);

  useEffect(() => {
    if (!token) {
      return;
    }

    const validateSession = () => {
      apiClient.get('api/v1/user/profile').catch(() => undefined);
    };

    validateSession();

    const appStateSubscription = AppState.addEventListener('change', nextState => {
      if (nextState === 'active') {
        validateSession();
      }
    });

    const interval = setInterval(() => {
      validateSession();
    }, 15000);

    return () => {
      appStateSubscription.remove();
      clearInterval(interval);
    };
  }, [token]);

  if (!isHydrated) {
    return (
      <View style={styles.bootContainer}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }

  return (
    <NavigationContainer ref={navigationRef}>
      <Stack.Navigator
        screenOptions={{
          headerStyle: {backgroundColor: colors.surface},
          headerTintColor: colors.text,
          headerTitleStyle: {fontWeight: '700', fontSize: 17},
          headerShadowVisible: false,
          contentStyle: {backgroundColor: colors.background},
        }}>
        {token ? (
          <>
            <Stack.Screen name="Home" component={HomeScreen} options={{headerShown: false}} />
            <Stack.Screen
              name="Search"
              getComponent={getSearchScreen}
              initialParams={{category: DEFAULT_CATEGORY}}
              options={{headerShown: false}}
            />
            <Stack.Screen name="HomeCards" getComponent={getHomeCardsScreen} options={{headerShown: false}} />
            <Stack.Screen name="Merchant" getComponent={getMerchantScreen} options={{headerShown: false}} />
            <Stack.Screen name="Product" getComponent={getProductScreen} options={{headerShown: false}} />
            <Stack.Screen name="Country" getComponent={getCountryScreen} options={{headerShown: false}} />
            <Stack.Screen name="Factory" getComponent={getFactoryScreen} options={{headerShown: false}} />
            <Stack.Screen
              name="CountryProduct"
              getComponent={getCountryProductScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="CountryFactoryProduct"
              getComponent={getCountryFactoryProductScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="SubstituteProduct"
              getComponent={getSubstituteProductScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="DataComparison"
              getComponent={getDataComparisonScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen name="Brand" getComponent={getBrandScreen} options={{headerShown: false}} />
            <Stack.Screen
              name="BrandProduct"
              getComponent={getBrandProductScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen name="Profile" getComponent={getProfileScreen} options={{headerShown: false}} />
            <Stack.Screen
              name="EditProfile"
              getComponent={getEditProfileScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen name="Inventory" getComponent={getInventoryScreen} options={{headerShown: false}} />
          </>
        ) : (
          <Stack.Screen name="Login" component={LoginScreen} options={{headerShown: false}} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  bootContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
