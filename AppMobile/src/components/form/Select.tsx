import React, { useRef, useState } from 'react'
import { Text, TouchableOpacity, FlatList, StyleSheet, View } from 'react-native'
import Ionicons from 'react-native-vector-icons/Ionicons'
import { Colors } from '@/constants/Colors'
import DraggableBottom, { DraggableBottomRef } from '../DraggableBottom'
import { WINDOW_HEIGHT } from '@/utils'

type Option = {
  label: any
  value: any
}

type Props = {
  options: Option[]
  value: any
  onChange: (val: any) => void
  placeholder?: string
  label?: string
}

const Select: React.FC<Props> = ({
  options,
  value,
  onChange,
  placeholder = '-- Vui lòng chọn --',
  label,
}) => {
  const [isShow, setIsShow] = useState(false)
  const bottomSheetRef = useRef<DraggableBottomRef>(null)

  const selectedItem = options.find((o) => o.value === value)
  const selected = selectedItem?.label || placeholder

  const handleDown = (item?: Option) => {
    if (item) {
      onChange(item.value)
    }
    bottomSheetRef.current?.close()
  }

  return (
    <View style={styles.container}>
      {label && (
        <Text numberOfLines={1} style={[styles.label, isShow ? { color: Colors.primary } : {}]}>
          {label}
        </Text>
      )}
      <TouchableOpacity
        style={[styles.selectBox, isShow ? { borderColor: Colors.primary } : {}]}
        onPress={() => {
          setIsShow(true)
        }}
      >
        <Text style={value == '' ? styles.placeholderText : styles.selectedText}>{selected}</Text>
        <Ionicons name="chevron-down" size={18} color="#888" style={styles.icon} />
      </TouchableOpacity>

      <DraggableBottom
        ref={bottomSheetRef}
        title={placeholder}
        isShow={isShow}
        onClose={() => setIsShow(false)}
      >
        <FlatList
          data={options}
          keyExtractor={(item) => item.value}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.option} onPress={() => handleDown(item)}>
              <Text style={{ marginRight: 10, color: '#333333', fontWeight: 500, flex: 1 }}>
                {item.label}
              </Text>
              {item.value === selectedItem?.value && (
                <Ionicons name="checkmark-sharp" size={20} color={Colors.primary} />
              )}
            </TouchableOpacity>
          )}
          style={{ minHeight: WINDOW_HEIGHT * 0.6 }}
        />
      </DraggableBottom>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 10,
  },
  selectBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    padding: 12,
    borderWidth: 1,
    borderRadius: 8,
    borderColor: '#ccc',
    backgroundColor: '#fff',
  },
  label: {
    backgroundColor: '#fff',
    marginBottom: -10,
    marginLeft: 10,
    zIndex: 1,
    padding: 10,
    paddingBottom: 0,
    paddingTop: 0,
    textAlign: 'left',
    alignSelf: 'flex-start',
    color: Colors.textSecondary,
    fontWeight: '500',
    fontSize: 13,
  },
  option: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 10,
    paddingBottom: 10,
    paddingLeft: 20,
    paddingRight: 20,
  },
  placeholderText: {
    color: '#999',
  },
  selectedText: {
    color: Colors.primary,
  },
  icon: {
    marginLeft: 8,
  },
})

export default Select
