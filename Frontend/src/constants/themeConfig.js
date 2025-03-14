const inputBgColor = '#f1f3f4'
const inputBdColor = '#dfdfdf'
const inputTxtColor = '#444'

export const themeConfig = {
  token: {
    colorPrimary: '#41395b',
    controlHeight: 38,
  },
  components: {
    Modal: {
      transitionName: 'zoom',
    },
    Input: {
      colorBgContainer: inputBgColor,
      colorBorder: inputBdColor,
      colorText: inputTxtColor,
    },
    Select: {
      colorBgContainer: inputBgColor,
      colorBorder: inputBdColor,
      colorText: inputTxtColor,
    },
    TextArea: {
      colorBgContainer: inputBgColor,
      colorBorder: inputBdColor,
      colorText: inputTxtColor,
    },
    InputNumber: {
      colorBgContainer: inputBgColor,
      colorBorder: inputBdColor,
      colorText: inputTxtColor,
    },
    DatePicker: {
      colorBgContainer: inputBgColor,
      colorBorder: inputBdColor,
      colorText: inputTxtColor,
    },
  },
}
