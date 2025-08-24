import { Colors } from '@/constants/Colors'
import { ItemNotification } from '@/types/ItemNotification'
import { formatDate } from '@/utils'
import { StyleSheet, useWindowDimensions, View } from 'react-native'
import { IconButton, Text } from 'react-native-paper'
import RenderHTML from 'react-native-render-html'

type Props = {
  item: ItemNotification
  index: number
  onOption: () => void
}

export const CollapseItemNotification = ({ index, item, onOption }: Props) => {
  const { width } = useWindowDimensions()

  return (
    <View style={styles.container}>
      <View style={[styles.item, { opacity: item.status === 1 ? 1 : 0.3 }]}>
        <View style={{ flex: 1, padding: 10, paddingRight: 20 }}>
          {item.status === 1 && <View style={styles.dot}></View>}
          <Text variant="bodySmall" style={{ opacity: 0.5, fontSize: 11 }}>
            {formatDate(item.createdAt, 'dd/MM/yyyy HH:mm')}
          </Text>
          <RenderHTML
            contentWidth={width}
            tagsStyles={{
              div: {
                color: Colors.primary,
              },
            }}
            source={{ html: `<div>${item.message}</div>` }}
          />
        </View>
        <IconButton icon="dots-horizontal" size={20} onPress={onOption} />
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 10,
    padding: 10,
    marginHorizontal: 15,
    marginTop: 10,
  },
  item: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  dot: {
    height: 6,
    width: 6,
    borderRadius: 999,
    backgroundColor: Colors.error,
    position: 'absolute',
    top: 0,
    left: 0,
  },
})
