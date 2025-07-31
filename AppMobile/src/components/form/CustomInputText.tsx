import React, { useState } from 'react'
import { View, TextInput, StyleSheet, Text } from 'react-native'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'
import { Colors } from '@/constants/Colors'

type Props = {
  value: string
  onChange: (val: string) => void
  placeholder?: string
  label?: string
  secureTextEntry?: boolean
  leftIcon?: string
  required?: boolean
}

const CustomInputText: React.FC<Props> = ({
  value,
  onChange,
  placeholder = 'Nhập...',
  label,
  secureTextEntry = false,
  leftIcon,
  required = false,
}) => {
  const [isFocus, setIsFocus] = useState(false)
  const isError = required && value === ''
  return (
    <View style={styles.container}>
      {label && (
        <Text
          numberOfLines={1}
          style={[
            styles.label,
            isFocus ? { color: Colors.primary } : {},
            isError ? { color: Colors.error } : {},
          ]}
        >
          {label}
        </Text>
      )}
      <View
        style={[
          styles.inputContainer,
          isFocus && { borderColor: Colors.primary },
          isError && { borderColor: Colors.error },
        ]}
      >
        {leftIcon && (
          <Icon
            name={leftIcon}
            size={20}
            color={isError ? Colors.error : isFocus ? Colors.primary : Colors.textSecondary}
            style={styles.icon}
          />
        )}
        <TextInput
          style={styles.input}
          placeholder={placeholder}
          placeholderTextColor="#999999"
          value={value}
          onChangeText={onChange}
          secureTextEntry={secureTextEntry}
          onFocus={() => setIsFocus(true)}
          onBlur={() => setIsFocus(false)}
        />
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    marginBottom: 10,
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
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    paddingHorizontal: 10,
    backgroundColor: '#fff',
    height: 50,
  },
  icon: {
    marginRight: 8,
  },
  input: {
    flex: 1,
    fontSize: 14,
    color: Colors.text,
  },
})

export default CustomInputText
