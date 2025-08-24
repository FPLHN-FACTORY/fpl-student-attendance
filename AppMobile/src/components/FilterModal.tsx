import React, { useRef, useState } from 'react'
import { Keyboard, StyleSheet, TouchableWithoutFeedback, View } from 'react-native'
import DraggableBottom, { DraggableBottomRef } from './DraggableBottom'
import { Button } from 'react-native-paper'
import { WINDOW_HEIGHT } from '@/utils'

type Props = {
  children: (closeModal: () => void) => React.ReactNode
  isFilter: boolean
  totalItems: number
}

const FilterModal: React.FC<Props> = ({ children, totalItems, isFilter }) => {
  const [isShow, setIsShow] = useState(false)
  const bottomSheetRef = useRef<DraggableBottomRef>(null)

  const handleShowFilter = () => {
    setIsShow(true)
  }

  const closeModal = () => {
    bottomSheetRef.current?.close?.()
  }

  return (
    <>
      <Button
        icon="filter"
        labelStyle={styles.buttonFilter}
        onPress={handleShowFilter}
        mode={isFilter ? 'contained' : 'text'}
      >
        Bộ lọc ({totalItems})
      </Button>

      <DraggableBottom
        ref={bottomSheetRef}
        title="Bộ lọc"
        isShow={isShow}
        onClose={() => setIsShow(false)}
      >
        <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
          <View style={styles.container}>{children(closeModal)}</View>
        </TouchableWithoutFeedback>
      </DraggableBottom>
    </>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    minHeight: WINDOW_HEIGHT,
  },
  buttonFilter: {
    fontSize: 11,
  },
})

export default FilterModal
