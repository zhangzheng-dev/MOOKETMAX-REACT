import React, {useEffect} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {ActivityIndicator, StyleSheet, View} from 'react-native';
import {DEFAULT_CATEGORY} from '../config/env';
import {BrandProductScreen} from '../screens/BrandProductScreen';
import {BrandScreen} from '../screens/BrandScreen';
import {CountryFactoryProductScreen} from '../screens/CountryFactoryProductScreen';
import {CountryProductScreen} from '../screens/CountryProductScreen';
import {CountryScreen} from '../screens/CountryScreen';
import {DataComparisonScreen} from '../screens/DataComparisonScreen';
import {EditProfileScreen} from '../screens/EditProfileScreen';
import {FactoryScreen} from '../screens/FactoryScreen';
import {HomeCardsScreen} from '../screens/HomeCardsScreen';
import {HomeScreen} from '../screens/HomeScreen';
import InventoryScreen from '../screens/InventoryScreen';
import {LoginScreen} from '../screens/LoginScreen';
import {MerchantScreen} from '../screens/MerchantScreen';
import {ProductScreen} from '../screens/ProductScreen';
import {ProfileScreen} from '../screens/ProfileScreen';
import {SearchScreen} from '../screens/SearchScreen';
import {SubstituteProductScreen} from '../screens/SubstituteProductScreen';
import {sessionStore} from '../store/sessionStore';
import {colors} from '../theme/colors';
import {navigationRef} from './navigationService';
import type {RootStackParamList} from './routes';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function AppNavigator() {
  const {hydrate, isHydrated, token} = sessionStore();

  useEffect(() => {
    hydrate();
  }, [hydrate]);

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
              component={SearchScreen}
              initialParams={{category: DEFAULT_CATEGORY}}
              options={{headerShown: false}}
            />
            <Stack.Screen name="HomeCards" component={HomeCardsScreen} options={{headerShown: false}} />
            <Stack.Screen name="Merchant" component={MerchantScreen} options={{headerShown: false}} />
            <Stack.Screen name="Product" component={ProductScreen} options={{headerShown: false}} />
            <Stack.Screen name="Country" component={CountryScreen} options={{headerShown: false}} />
            <Stack.Screen name="Factory" component={FactoryScreen} options={{headerShown: false}} />
            <Stack.Screen name="CountryProduct" component={CountryProductScreen} options={{headerShown: false}} />
            <Stack.Screen
              name="CountryFactoryProduct"
              component={CountryFactoryProductScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen name="SubstituteProduct" component={SubstituteProductScreen} options={{headerShown: false}} />
            <Stack.Screen name="DataComparison" component={DataComparisonScreen} options={{headerShown: false}} />
            <Stack.Screen name="Brand" component={BrandScreen} options={{headerShown: false}} />
            <Stack.Screen name="BrandProduct" component={BrandProductScreen} options={{headerShown: false}} />
            <Stack.Screen name="Profile" component={ProfileScreen} options={{headerShown: false}} />
            <Stack.Screen name="EditProfile" component={EditProfileScreen} options={{headerShown: false}} />
            <Stack.Screen name="Inventory" component={InventoryScreen} options={{headerShown: false}} />
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
