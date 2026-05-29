import React, {useEffect, useState} from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import Svg, {Path} from 'react-native-svg';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {launchImageLibrary} from 'react-native-image-picker';
import {mooketApi} from '../api/mooketApi';
import type {RootStackParamList} from '../navigation/routes';
import {sessionStore} from '../store/sessionStore';
import {colors} from '../theme/colors';

type Props = NativeStackScreenProps<RootStackParamList, 'EditProfile'>;

const identityOptions = ['海外供应商', '贸易商', '加工厂/商超', '其他'];

function normalizeIdentityTags(tags: string[] | null | undefined) {
  return (tags ?? []).map(tag => (tag === '海外服务商' ? '海外供应商' : tag));
}

export function EditProfileScreen({navigation}: Props) {
  const insets = useSafeAreaInsets();
  const {user, setUser} = sessionStore();
  const [nickname, setNickname] = useState(user?.nickname ?? '');
  const [identityTags, setIdentityTags] = useState<string[]>(
    normalizeIdentityTags(user?.identityTags),
  );
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(user?.avatarUrl ?? null);

  useEffect(() => {
    setNickname(user?.nickname ?? '');
    setIdentityTags(normalizeIdentityTags(user?.identityTags));
    setAvatarPreview(user?.avatarUrl ?? null);
  }, [user]);

  function toggleTag(tag: string) {
    setIdentityTags(prev => (prev.includes(tag) ? prev.filter(item => item !== tag) : [...prev, tag]));
  }

  async function save() {
    const name = nickname.trim();
    if (name.length < 2 || name.length > 20) {
      Alert.alert('提示', '昵称需要 2-20 个字符');
      return;
    }
    setSaving(true);
    try {
      await mooketApi.updateProfile({nickname: name, identityTags});
      const latest = await mooketApi.getUserProfile();
      await setUser(latest);
      navigation.goBack();
    } catch (error) {
      Alert.alert('保存失败', error instanceof Error ? error.message : '请稍后重试');
    } finally {
      setSaving(false);
    }
  }

  async function pickAvatar() {
    try {
      const result = await launchImageLibrary({
        mediaType: 'photo',
        selectionLimit: 1,
        includeBase64: false,
      });
      if (result.didCancel) return;
      if (result.errorMessage) {
        Alert.alert('选图失败', result.errorMessage);
        return;
      }
      const asset = result.assets?.[0];
      if (!asset?.uri) {
        Alert.alert('选图失败', '没有读取到图片，请重试');
        return;
      }
      setAvatarPreview(asset.uri);
      setUploading(true);
      await mooketApi.uploadAvatar({
        uri: asset.uri,
        type: asset.type ?? 'image/jpeg',
        name: asset.fileName ?? `avatar-${Date.now()}.jpg`,
      });
      const latest = await mooketApi.getUserProfile();
      await setUser(latest);
      setAvatarPreview(latest.avatarUrl ?? asset.uri);
    } catch (error) {
      setAvatarPreview(user?.avatarUrl ?? null);
      Alert.alert('上传失败', error instanceof Error ? error.message : '请稍后重试');
    } finally {
      setUploading(false);
    }
  }

  const valid = nickname.trim().length >= 2 && identityTags.length > 0;

  return (
    <View style={styles.container}>
      <View style={[styles.header, {paddingTop: insets.top, minHeight: insets.top + 56}]}>
        <Pressable hitSlop={8} onPress={() => navigation.goBack()} style={styles.headerButton}>
          <Svg width={20} height={20} viewBox="0 0 24 24" fill="none">
            <Path
              d="M15 5L8 12L15 19"
              stroke={colors.text}
              strokeWidth={2}
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </Svg>
        </Pressable>
        <Text style={styles.headerTitle}>编辑资料</Text>
        <Pressable
          hitSlop={8}
          disabled={!valid || saving}
          onPress={save}
          style={styles.headerButton}>
          {saving ? (
            <ActivityIndicator color={colors.primary} size="small" />
          ) : (
            <Text style={[styles.saveText, !valid && styles.saveTextDisabled]}>保存</Text>
          )}
        </Pressable>
      </View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled">
        <View style={styles.avatarSection}>
          <Pressable
            disabled={uploading}
            onPress={() => pickAvatar().catch(() => undefined)}
            style={styles.avatarWrap}>
            {avatarPreview ? (
              <Image source={{uri: avatarPreview}} style={styles.avatarImage} />
            ) : (
              <Text style={styles.avatarPlaceholder}>{(nickname || '牧').slice(0, 1)}</Text>
            )}
            {uploading ? (
              <View style={styles.avatarOverlay}>
                <ActivityIndicator color="#FFFFFF" />
              </View>
            ) : null}
          </Pressable>
          <Text style={styles.avatarHint}>点击更换头像</Text>
        </View>

        <Text style={styles.sectionTitle}>基本信息</Text>
        <View style={styles.card}>
          <View style={styles.fieldRow}>
            <Text style={styles.fieldLabel}>姓名</Text>
            <View style={styles.fieldValueRow}>
              <Text style={styles.fieldValue}>{user?.realName ?? '--'}</Text>
              {user?.realNameStatus === 'verified' || user?.realName ? (
                <View style={styles.verifiedBadge}>
                  <Svg width={12} height={12} viewBox="0 0 12 12" fill="none">
                    <Path
                      d="M6 1L7.5 2.5L9.5 2L9 4L11 5.5L9.5 7L10 9L8 8.5L6 10.5L4 8.5L2 9L2.5 7L1 5.5L3 4L2.5 2L4.5 2.5L6 1Z"
                      fill="#006A61"
                    />
                    <Path
                      d="M4 6L5.5 7.5L8 4.5"
                      stroke="white"
                      strokeWidth={1.2}
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </Svg>
                  <Text style={styles.verifiedText}>已认证</Text>
                </View>
              ) : null}
            </View>
          </View>
          <View style={styles.divider} />
          <View style={styles.fieldRow}>
            <Text style={styles.fieldLabel}>昵称</Text>
            <TextInput
              value={nickname}
              onChangeText={setNickname}
              maxLength={20}
              placeholder="请输入昵称"
              placeholderTextColor={colors.textMuted}
              style={styles.fieldInput}
            />
          </View>
          <View style={styles.divider} />
          <View style={styles.fieldRow}>
            <Text style={styles.fieldLabel}>手机号</Text>
            <Text style={styles.fieldValue}>{user?.phone ?? '--'}</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.fieldRow}>
            <Text style={styles.fieldLabel}>牧集号</Text>
            <Text style={styles.fieldValue}>{user?.mooketNo ?? user?.mooketId ?? '--'}</Text>
          </View>
        </View>

        <Text style={styles.sectionTitle}>行业身份</Text>
        <View style={styles.tagWrap}>
          {identityOptions.map(tag => {
            const active = identityTags.includes(tag);
            return (
              <Pressable
                key={tag}
                onPress={() => toggleTag(tag)}
                style={[styles.tag, active && styles.tagActive]}>
                <Text style={[styles.tagText, active && styles.tagTextActive]}>{tag}</Text>
              </Pressable>
            );
          })}
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: colors.background},
  header: {
    paddingHorizontal: 16,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  headerButton: {
    minWidth: 40,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitle: {color: colors.text, fontSize: 18, fontWeight: '600'},
  saveText: {color: colors.primary, fontSize: 14, fontWeight: '600'},
  saveTextDisabled: {color: colors.textMuted},
  scroll: {flex: 1},
  content: {padding: 16, gap: 16, paddingBottom: 48},
  avatarSection: {alignItems: 'center', gap: 8},
  avatarWrap: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  avatarImage: {width: '100%', height: '100%'},
  avatarPlaceholder: {color: colors.primary, fontSize: 28, fontWeight: '700'},
  avatarOverlay: {
    position: 'absolute',
    inset: 0 as never,
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0,0,0,0.35)',
  },
  avatarHint: {color: colors.textMuted, fontSize: 12},
  sectionTitle: {color: colors.text, fontSize: 15, fontWeight: '600', marginTop: 8},
  card: {
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 16,
  },
  fieldRow: {
    minHeight: 52,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  fieldLabel: {color: colors.text, fontSize: 14, width: 64},
  fieldValue: {flex: 1, color: colors.textSecondary, fontSize: 14, textAlign: 'right'},
  fieldValueRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 6,
  },
  verifiedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    paddingHorizontal: 4,
    paddingVertical: 2,
    borderRadius: 2,
    backgroundColor: 'rgba(0,106,97,0.08)',
  },
  verifiedText: {color: colors.primary, fontSize: 10, fontWeight: '500'},
  fieldInput: {
    flex: 1,
    color: colors.text,
    fontSize: 14,
    textAlign: 'right',
    paddingVertical: 0,
  },
  divider: {height: StyleSheet.hairlineWidth, backgroundColor: colors.border},
  tagWrap: {flexDirection: 'row', flexWrap: 'wrap', gap: 12},
  tag: {
    minWidth: 80,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: 'rgba(0,106,97,0.15)',
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tagActive: {borderColor: colors.primary, backgroundColor: 'rgba(0,106,97,0.05)'},
  tagText: {color: '#3C4947', fontSize: 14},
  tagTextActive: {color: colors.primary, fontWeight: '600'},
});
