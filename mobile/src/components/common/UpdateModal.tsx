import React, {useState} from 'react';
import {
  ActivityIndicator,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import ReactNativeBlobUtil from 'react-native-blob-util';
import {colors} from '../../theme/colors';
import type {AppVersionInfo} from '../../types/api';

type Props = {
  visible: boolean;
  versionInfo: AppVersionInfo;
  onClose: () => void;
};

export function UpdateModal({visible, versionInfo, onClose}: Props) {
  const [downloading, setDownloading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);

  async function handleConfirm() {
    if (!versionInfo.updateUrl) {
      setError('下载地址为空');
      return;
    }

    setDownloading(true);
    setProgress(0);
    setError(null);

    try {
      const dirs = ReactNativeBlobUtil.fs.dirs;
      const safeVersion = (versionInfo.version ?? 'unknown').replace(/[^0-9A-Za-z._-]/g, '_');
      const safeVersionCode = String(versionInfo.versionCode ?? '0').replace(/[^0-9]/g, '');
      const filePath = `${dirs.CacheDir}/mooket_update_${safeVersion}_v${safeVersionCode}.apk`;

      // Remove stale cached packages first. Some Android installers cache APK metadata by path.
      try {
        const names = await ReactNativeBlobUtil.fs.ls(dirs.CacheDir);
        const staleFiles = names.filter(name => /^mooket_update_.*\.apk$/i.test(name));
        await Promise.all(
          staleFiles.map(name =>
            ReactNativeBlobUtil.fs.unlink(`${dirs.CacheDir}/${name}`).catch(() => undefined),
          ),
        );
      } catch {
        // Ignore cache cleanup failures and continue.
      }

      const exists = await ReactNativeBlobUtil.fs.exists(filePath);
      if (exists) {
        await ReactNativeBlobUtil.fs.unlink(filePath);
      }

      const res = await ReactNativeBlobUtil.config({
        path: filePath,
        fileCache: true,
      })
        .fetch('GET', versionInfo.updateUrl, {})
        .progress({count: 10}, (received, total) => {
          const pct = Math.round((Number(received) / Number(total)) * 100);
          setProgress(pct);
        });

      const savedPath = res.path();
      setProgress(100);

      if (Platform.OS === 'android') {
        await ReactNativeBlobUtil.android.actionViewIntent(
          savedPath,
          'application/vnd.android.package-archive',
        );
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '下载失败');
    } finally {
      setDownloading(false);
    }
  }

  function handleClose() {
    if (!downloading) {
      onClose();
    }
  }

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
      <View style={styles.overlay}>
        <View style={styles.card}>
          <Text style={styles.title}>发现新版本</Text>
          <Text style={styles.version}>v{versionInfo.version}</Text>

          {versionInfo.updateContent ? (
            <View style={styles.contentWrap}>
              <Text style={styles.contentLabel}>更新内容：</Text>
              <Text style={styles.contentText}>{versionInfo.updateContent}</Text>
            </View>
          ) : null}

          {downloading ? (
            <View style={styles.progressWrap}>
              <View style={styles.progressBg}>
                <View style={[styles.progressBar, {width: `${progress}%`}]} />
              </View>
              <Text style={styles.progressText}>{progress}%</Text>
            </View>
          ) : error ? (
            <Text style={styles.errorText}>{error}</Text>
          ) : null}

          {!downloading ? (
            <View style={styles.buttons}>
              <Pressable onPress={handleClose} style={styles.cancelButton}>
                <Text style={styles.cancelText}>取消</Text>
              </Pressable>
              <Pressable onPress={handleConfirm} style={styles.confirmButton}>
                <Text style={styles.confirmText}>立即更新</Text>
              </Pressable>
            </View>
          ) : (
            <View style={styles.downloadingHint}>
              <ActivityIndicator size="small" color={colors.primary} />
              <Text style={styles.downloadingText}>正在下载，请勿关闭...</Text>
            </View>
          )}
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  card: {
    width: '100%',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    paddingHorizontal: 24,
    paddingVertical: 28,
    gap: 16,
  },
  title: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '600',
    textAlign: 'center',
  },
  version: {
    color: colors.primary,
    fontSize: 14,
    fontWeight: '500',
    textAlign: 'center',
  },
  contentWrap: {gap: 4},
  contentLabel: {color: '#3C4947', fontSize: 13, fontWeight: '500'},
  contentText: {color: '#6C7A77', fontSize: 13, lineHeight: 20},
  progressWrap: {gap: 8, alignItems: 'center'},
  progressBg: {
    width: '100%',
    height: 8,
    borderRadius: 4,
    backgroundColor: '#EFF5F3',
    overflow: 'hidden',
  },
  progressBar: {
    height: '100%',
    borderRadius: 4,
    backgroundColor: colors.primary,
  },
  progressText: {color: colors.primary, fontSize: 13, fontWeight: '600'},
  errorText: {color: '#A53321', fontSize: 13, textAlign: 'center'},
  buttons: {flexDirection: 'row', gap: 12, marginTop: 4},
  cancelButton: {
    flex: 1,
    height: 44,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#DEE4E1',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelText: {color: '#3C4947', fontSize: 15, fontWeight: '500'},
  confirmButton: {
    flex: 1,
    height: 44,
    borderRadius: 6,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  confirmText: {color: '#FFFFFF', fontSize: 15, fontWeight: '500'},
  downloadingHint: {flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8},
  downloadingText: {color: '#6C7A77', fontSize: 13},
});
