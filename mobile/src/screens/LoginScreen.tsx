import React, {useEffect, useMemo, useRef, useState} from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {mooketApi} from '../api/mooketApi';
import {
  ArrowLeftIcon,
  CheckMarkIcon,
  ClearInputIcon,
  CtccIcon,
  MooketMaxLogo,
} from '../components/login/LoginIcons';
import type {RootStackParamList} from '../navigation/routes';
import {sessionStore} from '../store/sessionStore';
import {colors} from '../theme/colors';
import type {AuthResult, SendCodeResult} from '../types/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

async function getDeviceId(): Promise<string> {
  const key = 'mooket.device_id';
  let id = await AsyncStorage.getItem(key);
  if (!id) {
    id = `rn-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    await AsyncStorage.setItem(key, id);
  }
  return id;
}

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>;
type Step = 'phone' | 'verify' | 'oneClick' | 'register';

const identityOptions = ['海外服务商', '贸易商', '加工厂/商超', '其他'];

export function LoginScreen({navigation}: Props) {
  const {setAuth, setUser} = sessionStore();
  const [step, setStep] = useState<Step>('phone');
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [agreement, setAgreement] = useState(true);
  const [countdown, setCountdown] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [nickname, setNickname] = useState('');
  const [identityTags, setIdentityTags] = useState<string[]>([]);
  const [pendingAuth, setPendingAuth] = useState<AuthResult | null>(null);
  const [sendCodeResult, setSendCodeResult] = useState<SendCodeResult | null>(null);
  const codeInputRef = useRef<TextInput>(null);

  useEffect(() => {
    if (step !== 'verify' || countdown <= 0) return;
    const timer = setTimeout(() => setCountdown(prev => Math.max(prev - 1, 0)), 1000);
    return () => clearTimeout(timer);
  }, [countdown, step]);

  useEffect(() => {
    if (step === 'verify') {
      const handle = setTimeout(() => codeInputRef.current?.focus(), 200);
      return () => clearTimeout(handle);
    }
    return undefined;
  }, [step]);

  async function finishLogin(auth: AuthResult) {
    await setAuth(auth);
    try {
      const profile = await mooketApi.getUserProfile();
      await setUser(profile);
    } catch {
      await setUser(null);
    }
    navigation.reset({index: 0, routes: [{name: 'Home'}]});
  }

  async function handleSendCode(targetPhone = phone) {
    if (targetPhone.length !== 11) {
      setError('请输入 11 位手机号');
      return;
    }
    if (!agreement) {
      setError('请先阅读并同意服务条款与隐私政策');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const devId = await getDeviceId();
      const result = await mooketApi.sendCode(targetPhone, devId);
      setSendCodeResult(result);
      setCode('');
      setCountdown(60);
      setStep('verify');
    } catch (e) {
      setError(e instanceof Error ? e.message : '验证码发送失败');
    } finally {
      setLoading(false);
    }
  }

  async function handleLogin(value = code) {
    if (value.length !== 6) {
      setError('请输入 6 位验证码');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const devId = await getDeviceId();
      const auth = await mooketApi.login(phone, value, devId);
      if (auth.isNewUser) {
        setPendingAuth(auth);
        setNickname(auth.nickname ?? '');
        setIdentityTags([]);
        setStep('register');
        return;
      }
      await finishLogin(auth);
    } catch (e) {
      const msg = e instanceof Error ? e.message : '登录失败，请稍后重试';
      setError(`${msg}（请检查网络后重试）`);
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister() {
    const name = nickname.trim();
    if (name.length < 2 || name.length > 20) {
      setError('昵称需要 2-20 个字符');
      return;
    }
    if (identityTags.length === 0) {
      setError('请至少选择一个身份标签');
      return;
    }
    if (!pendingAuth?.token || !pendingAuth.gatewayAccessToken) {
      setError('注册会话已失效，请重新获取验证码');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const auth = await mooketApi.register(pendingAuth.token, {
        nickname: name,
        identityTags,
        gatewayAccessToken: pendingAuth.gatewayAccessToken,
        code,
        clientId: sendCodeResult?.clientId ?? null,
      });
      await finishLogin(auth);
    } catch (e) {
      setError(e instanceof Error ? e.message : '注册失败');
    } finally {
      setLoading(false);
    }
  }

  function handleOneClickEntry() {
    if (phone.length !== 11) {
      Alert.alert('提示', '一键登录需要先输入手机号或先经短信验证。');
      return;
    }
    setStep('oneClick');
  }

  function handleBack() {
    if (step === 'register') {
      setStep('verify');
    } else if (step === 'verify' || step === 'oneClick') {
      setStep('phone');
    }
    setError(null);
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.container}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        {step === 'phone' ? (
          <PhoneStep
            phone={phone}
            agreement={agreement}
            loading={loading}
            error={error}
            onPhoneChange={value => {
              setPhone(value.replace(/\D/g, '').slice(0, 11));
              setError(null);
            }}
            onAgreementToggle={() => setAgreement(prev => !prev)}
            onSubmit={() => handleSendCode()}
            onOneClick={handleOneClickEntry}
          />
        ) : null}

        {step === 'verify' ? (
          <VerifyStep
            phone={phone}
            code={code}
            countdown={countdown}
            loading={loading}
            error={error}
            inputRef={codeInputRef}
            onBack={handleBack}
            onCodeChange={value => {
              const digits = value.replace(/\D/g, '').slice(0, 6);
              setCode(digits);
              setError(null);
              if (digits.length === 6) handleLogin(digits);
            }}
            onResend={() => handleSendCode()}
            onConfirm={() => handleLogin()}
          />
        ) : null}

        {step === 'oneClick' ? (
          <OneClickStep
            phone={phone}
            loading={loading}
            onBack={handleBack}
            onLogin={() => handleSendCode()}
            onOtherLogin={handleBack}
          />
        ) : null}

        {step === 'register' ? (
          <RegisterStep
            nickname={nickname}
            identityTags={identityTags}
            loading={loading}
            error={error}
            onBack={handleBack}
            onNicknameChange={value => {
              setNickname(value.slice(0, 20));
              setError(null);
            }}
            onTagToggle={tag => {
              setIdentityTags(prev =>
                prev.includes(tag) ? prev.filter(item => item !== tag) : [...prev, tag],
              );
              setError(null);
            }}
            onSubmit={handleRegister}
          />
        ) : null}
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function PhoneStep({
  phone,
  agreement,
  loading,
  error,
  onPhoneChange,
  onAgreementToggle,
  onSubmit,
  onOneClick,
}: {
  phone: string;
  agreement: boolean;
  loading: boolean;
  error: string | null;
  onPhoneChange: (value: string) => void;
  onAgreementToggle: () => void;
  onSubmit: () => void;
  onOneClick: () => void;
}) {
  const valid = phone.length === 11 && agreement;
  return (
    <View style={styles.content}>
      <View style={styles.logoSpacer} />
      <View style={styles.logoWrap}>
        <MooketMaxLogo />
      </View>

      <View style={styles.titleBlock}>
        <Text style={styles.title}>欢迎来到MooketMax</Text>
        <Text style={styles.subtitle}>未注册绑定的手机号将自动注册</Text>
      </View>

      <Text style={styles.fieldLabel}>手机号</Text>
      <View style={styles.underlineField}>
        <TextInput
          value={phone}
          onChangeText={onPhoneChange}
          keyboardType="number-pad"
          maxLength={11}
          placeholder="输入手机号用于登录/注册"
          placeholderTextColor={colors.textMuted}
          style={styles.phoneInput}
        />
        {phone.length > 0 ? (
          <Pressable hitSlop={8} onPress={() => onPhoneChange('')} style={styles.clearButton}>
            <ClearInputIcon size={18} />
          </Pressable>
        ) : null}
      </View>

      <Pressable onPress={onAgreementToggle} style={styles.agreementRow}>
        <View style={[styles.checkbox, agreement && styles.checkboxChecked]}>
          {agreement ? <CheckMarkIcon size={10} /> : null}
        </View>
        <Text style={styles.agreementText}>
          已阅读并同意
          <Text style={styles.link}> 服务条款 </Text>
          和
          <Text style={styles.link}> 隐私政策</Text>
        </Text>
      </Pressable>

      <Pressable
        onPress={onSubmit}
        disabled={!valid || loading}
        style={[styles.primaryButton, (!valid || loading) && styles.primaryButtonDisabled]}>
        {loading ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.primaryText}>登录</Text>}
      </Pressable>

      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <View style={styles.flex} />

      <View style={styles.thirdParty}>
        <Text style={styles.thirdPartyTitle}>第三方登录</Text>
        <Pressable onPress={onOneClick} style={styles.thirdPartyButton}>
          <CtccIcon />
        </Pressable>
      </View>
    </View>
  );
}

function VerifyStep({
  phone,
  code,
  countdown,
  loading,
  error,
  inputRef,
  onBack,
  onCodeChange,
  onResend,
  onConfirm,
}: {
  phone: string;
  code: string;
  countdown: number;
  loading: boolean;
  error: string | null;
  inputRef: React.RefObject<TextInput | null>;
  onBack: () => void;
  onCodeChange: (value: string) => void;
  onResend: () => void;
  onConfirm: () => void;
}) {
  const masked = useMemo(() => {
    if (phone.length !== 11) return phone;
    return `${phone.slice(0, 3)} ${phone.slice(3, 7)} ${phone.slice(7)}`;
  }, [phone]);

  return (
    <View style={styles.content}>
      <Pressable hitSlop={8} onPress={onBack} style={styles.backRow}>
        <ArrowLeftIcon />
      </Pressable>

      <Text style={styles.title}>输入验证码</Text>

      <View style={styles.verifyTopRow}>
        <Text style={styles.subtitle}>短信验证码已发送至</Text>
        <Pressable disabled={countdown > 0 || loading} onPress={onResend}>
          <Text style={[styles.resend, countdown > 0 && styles.resendDisabled]}>
            {countdown > 0 ? `${countdown}s后重新发送` : '重新发送'}
          </Text>
        </Pressable>
      </View>
      <Text style={styles.phoneHint}>+86 {masked}</Text>

      <Pressable style={styles.codeRow} onPress={() => inputRef.current?.focus()}>
        {[0, 1, 2, 3, 4, 5].map(index => {
          const focused = code.length === index;
          return (
            <View
              key={index}
              style={[styles.codeBox, focused && styles.codeBoxFocused]}>
              <Text style={styles.codeText}>{code[index] ?? ''}</Text>
            </View>
          );
        })}
      </Pressable>
      <TextInput
        ref={inputRef}
        value={code}
        onChangeText={onCodeChange}
        keyboardType="number-pad"
        maxLength={6}
        style={styles.hiddenInput}
        caretHidden
      />

      {error ? (
        <View style={styles.errorBadge}>
          <Text style={styles.errorBadgeText}>{error}</Text>
        </View>
      ) : null}

      <Pressable
        onPress={onConfirm}
        disabled={code.length !== 6 || loading}
        style={[styles.primaryButton, (code.length !== 6 || loading) && styles.primaryButtonDisabled]}>
        {loading ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.primaryText}>确认</Text>}
      </Pressable>

      <View style={styles.flex} />

      <View style={styles.thirdParty}>
        <Text style={styles.thirdPartyTitle}>第三方登录</Text>
        <View style={styles.thirdPartyButton}>
          <CtccIcon />
        </View>
      </View>
    </View>
  );
}

function OneClickStep({
  phone,
  loading,
  onBack,
  onLogin,
  onOtherLogin,
}: {
  phone: string;
  loading: boolean;
  onBack: () => void;
  onLogin: () => void;
  onOtherLogin: () => void;
}) {
  return (
    <View style={styles.content}>
      <Pressable hitSlop={8} onPress={onBack} style={styles.backRow}>
        <ArrowLeftIcon />
      </Pressable>
      <View style={[styles.logoWrap, styles.oneClickLogo]}>
        <MooketMaxLogo />
      </View>

      <View style={styles.oneClickPhoneWrap}>
        <Text style={styles.oneClickPhone}>{phone}</Text>
        <Text style={styles.oneClickHint}>天翼账号提供认证服务</Text>
      </View>

      <View style={styles.flex} />

      <Pressable
        onPress={onLogin}
        disabled={loading}
        style={[styles.primaryButton, loading && styles.primaryButtonDisabled]}>
        {loading ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.primaryText}>本机号码一键登录</Text>}
      </Pressable>
      <Pressable onPress={onOtherLogin} style={styles.otherLoginButton}>
        <Text style={styles.otherLoginText}>其他手机号登录</Text>
      </Pressable>

      <View style={styles.thirdParty}>
        <Text style={styles.thirdPartyTitle}>第三方登录</Text>
        <View style={styles.thirdPartyButton}>
          <CtccIcon />
        </View>
      </View>
    </View>
  );
}

function RegisterStep({
  nickname,
  identityTags,
  loading,
  error,
  onBack,
  onNicknameChange,
  onTagToggle,
  onSubmit,
}: {
  nickname: string;
  identityTags: string[];
  loading: boolean;
  error: string | null;
  onBack: () => void;
  onNicknameChange: (value: string) => void;
  onTagToggle: (tag: string) => void;
  onSubmit: () => void;
}) {
  const valid = nickname.trim().length >= 2 && identityTags.length > 0;
  return (
    <View style={styles.registerContent}>
      <Pressable hitSlop={8} onPress={onBack} style={styles.backRow}>
        <ArrowLeftIcon />
      </Pressable>

      <Text style={styles.registerTitle}>为了更好地向您提供</Text>
      <Text style={styles.registerTitle}>
        <Text style={styles.registerTitleAccent}>数据</Text>与
        <Text style={styles.registerTitleAccent}>服务</Text>
      </Text>

      <View style={styles.registerBadge}>
        <Text style={styles.registerBadgeText}>请填写</Text>
      </View>

      <Text style={[styles.fieldLabel, styles.registerLabelTop]}>您的昵称</Text>
      <TextInput
        value={nickname}
        onChangeText={onNicknameChange}
        maxLength={20}
        placeholder="输入您的昵称"
        placeholderTextColor={colors.textMuted}
        style={styles.registerInput}
      />

      <Text style={[styles.fieldLabel, styles.registerLabelMid]}>您的行业身份</Text>
      <View style={styles.identityWrap}>
        {identityOptions.map(tag => {
          const active = identityTags.includes(tag);
          return (
            <Pressable
              key={tag}
              onPress={() => onTagToggle(tag)}
              style={[styles.identityChip, active && styles.identityChipActive]}>
              <Text style={[styles.identityText, active && styles.identityTextActive]}>{tag}</Text>
            </Pressable>
          );
        })}
      </View>

      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <View style={styles.flex} />

      <Pressable
        onPress={onSubmit}
        disabled={!valid || loading}
        style={[styles.primaryButton, (!valid || loading) && styles.primaryButtonDisabled]}>
        {loading ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.primaryText}>确认</Text>}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {flex: 1, backgroundColor: colors.surface},
  container: {flex: 1, backgroundColor: colors.surface},
  content: {flex: 1, paddingHorizontal: 29, paddingTop: 12},
  registerContent: {flex: 1, paddingHorizontal: 30, paddingTop: 12, backgroundColor: '#F6FFFB'},
  flex: {flex: 1},
  backRow: {height: 36, justifyContent: 'center'},
  logoSpacer: {height: 84},
  logoWrap: {marginBottom: 8},
  oneClickLogo: {marginTop: 96, alignSelf: 'center'},
  titleBlock: {marginTop: 56, marginBottom: 24},
  title: {color: colors.text, fontSize: 24, fontWeight: '700'},
  subtitle: {marginTop: 8, color: colors.textSecondary, fontSize: 14},
  fieldLabel: {color: colors.text, fontSize: 14, marginBottom: 8},
  underlineField: {
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 6,
  },
  phoneInput: {
    flex: 1,
    color: colors.text,
    fontSize: 22,
    fontWeight: '600',
    paddingVertical: 6,
  },
  clearButton: {paddingLeft: 8},
  agreementRow: {
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
  },
  checkbox: {
    width: 14,
    height: 14,
    borderRadius: 2,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {borderColor: colors.primary, backgroundColor: colors.primary},
  agreementText: {marginLeft: 8, color: '#333333', fontSize: 13},
  link: {color: '#12877C'},
  primaryButton: {
    marginTop: 24,
    height: 44,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.primary,
  },
  primaryButtonDisabled: {backgroundColor: 'rgba(0,106,97,0.4)'},
  primaryText: {color: '#FFFFFF', fontSize: 16, fontWeight: '500'},
  errorText: {marginTop: 12, color: colors.danger, fontSize: 12, textAlign: 'center'},
  errorBadge: {
    marginTop: 12,
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 4,
    backgroundColor: 'rgba(247,114,52,0.1)',
  },
  errorBadgeText: {color: '#F77234', fontSize: 12},
  thirdParty: {alignItems: 'center', paddingVertical: 24},
  thirdPartyTitle: {color: '#3C4947', fontSize: 14},
  thirdPartyButton: {
    marginTop: 12,
    width: 60,
    height: 60,
    alignItems: 'center',
    justifyContent: 'center',
  },
  verifyTopRow: {
    marginTop: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  resend: {color: colors.primary, fontSize: 14},
  resendDisabled: {opacity: 0.7},
  phoneHint: {marginTop: 5, color: colors.text, fontSize: 17, fontWeight: '600'},
  codeRow: {marginTop: 32, flexDirection: 'row', gap: 8},
  codeBox: {
    flex: 1,
    height: 56,
    borderWidth: 1,
    borderColor: '#F5F5F5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  codeBoxFocused: {borderColor: colors.primary, borderWidth: 2},
  codeText: {fontSize: 30, fontWeight: '500', color: colors.text},
  hiddenInput: {position: 'absolute', width: 1, height: 1, opacity: 0},
  oneClickPhoneWrap: {marginTop: 64, alignItems: 'center', gap: 12},
  oneClickPhone: {color: colors.text, fontSize: 23, fontWeight: '600'},
  oneClickHint: {color: '#3C4947', fontSize: 12},
  otherLoginButton: {height: 44, alignItems: 'center', justifyContent: 'center', marginTop: 16},
  otherLoginText: {color: '#3C4947', fontSize: 16},
  registerTitle: {color: colors.text, fontSize: 26, fontWeight: '600', marginTop: 12},
  registerTitleAccent: {color: colors.primary},
  registerBadge: {
    marginTop: 12,
    alignSelf: 'flex-start',
    backgroundColor: '#00AEA0',
    paddingHorizontal: 6,
    paddingVertical: 4,
    borderRadius: 4,
  },
  registerBadgeText: {color: '#FFFFFF', fontSize: 12},
  registerLabelTop: {marginTop: 35},
  registerLabelMid: {marginTop: 24},
  registerInput: {
    height: 44,
    borderRadius: 2,
    borderWidth: 1,
    borderColor: 'rgba(0,106,97,0.15)',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    color: colors.text,
    fontSize: 15,
  },
  identityWrap: {flexDirection: 'row', flexWrap: 'wrap', gap: 16, marginBottom: 16},
  identityChip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 2,
    borderWidth: 1,
    borderColor: 'rgba(0,106,97,0.15)',
    backgroundColor: '#FFFFFF',
    minWidth: 80,
    alignItems: 'center',
  },
  identityChipActive: {
    borderColor: colors.primary,
    backgroundColor: 'rgba(0,106,97,0.05)',
  },
  identityText: {color: '#3C4947', fontSize: 15},
  identityTextActive: {color: colors.primary},
});
