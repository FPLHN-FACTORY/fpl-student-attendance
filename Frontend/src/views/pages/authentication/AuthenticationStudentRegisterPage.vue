<script setup>
import { useRouter } from 'vue-router'
import imgLogoFpt from '@/assets/images/logo-fpt.png'
import imgLogoUdpm from '@/assets/images/logo-udpm.png'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue3-toastify'
import requestAPI from '@/services/requestApiService'
import useAuthStore from '@/stores/useAuthStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'

import { message, Modal } from 'ant-design-vue'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import useFaceIDStore from '@/stores/useFaceIDStore'

const router = useRouter()

const authStore = useAuthStore()
const loadingPage = useLoadingStore()
const faceIDStore = useFaceIDStore()

const isShowCamera = ref(false)
const video = ref(null)
const canvas = ref(null)

const formData = reactive({
  idFacility: null,
  name: authStore.user.name,
  code: authStore.user.code,
  faceEmbedding: null,
})

const lstFacility = ref([])

const handleLogout = () => {
  authStore.logout()
  window.location.reload()
}

const formRules = reactive({
  idFacility: [{ required: true, message: 'Vui lòng chọn 1 cơ sở!' }],
  name: [{ required: true, message: 'Vui lòng nhập họ và tên!' }],
  code: [{ required: true, message: 'Vui lòng nhập mã số sinh viên!' }],
  faceEmbedding: [{ required: true, message: 'Vui lòng đăng ký khuôn mặt!' }],
})

const handleShowCamera = async () => {
  isShowCamera.value = true
  await nextTick()
  await faceIDStore.startVideo()
}

const handleSubmitRegister = async () => {
  Modal.confirm({
    title: `Xác nhận đăng ký sinh viên`,
    type: 'warning',
    content: `Thông tin không thể thay đổi. Bạn có muốn kiểm tra lại`,
    okText: 'Tiếp tục',
    cancelText: 'Hủy bỏ',
    onOk() {
      fetchSubmitRegister()
    },
  })
}

const fetchDataFacility = async () => {
  try {
    const response = await requestAPI.get(ROUTE_NAMES_API.FETCH_DATA_FACILITY)
    lstFacility.value = response.data.data
  } catch (error) {
    toast.error('Không thể tải danh sách cơ sở')
  }
}

const fetchSubmitRegister = () => {
  loadingPage.show()
  requestAPI
    .put(`${ROUTE_NAMES_API.FETCH_DATA_REGISTER}`, formData)
    .then(({ data: response }) => {
      message.success(response.message)
      authStore.updateUser({
        facilityID: formData.idFacility,
        name: formData.name,
        code: formData.code,
      })
      authStore.setToken(response.data.accessToken, response.data.refreshToken)
      router.push({ name: GLOBAL_ROUTE_NAMES.STUDENT_PAGE })
    })
    .catch((error) => {
      message.error(error?.response?.data?.message || 'Không thể đăng ký thông tin sinh viên')
    })
    .finally(() => {
      loadingPage.hide()
    })
}

onMounted(async () => {
  document.body.classList.add('bg-login')
  fetchDataFacility()
  faceIDStore.init(video, canvas, false, (descriptor) => {
    formData.faceEmbedding = JSON.stringify(descriptor)
    isShowCamera.value = false
  })
  await faceIDStore.loadModels()
})
</script>

<template>
  <a-modal
    v-model:open="isShowCamera"
    title="Checkin khuôn mặt"
    @cancel="faceIDStore.stopVideo()"
    :footer="null"
  >
    <div class="video-container">
      <canvas ref="canvas"></canvas>
      <video ref="video" autoplay muted></video>
      <div class="face-id-step" :class="faceIDStore.renderStyle()">
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
      </div>
      <div class="face-id-loading" v-show="faceIDStore.isLoading">
        <div class="bg-loading">
          <div></div>
          <div></div>
          <div></div>
        </div>
      </div>
    </div>
    <div class="face-id-text" v-show="faceIDStore.textStep != null">
      {{ faceIDStore.textStep }}
    </div>
  </a-modal>

  <div class="container">
    <div class="row">
      <div class="logo">
        <img :src="imgLogoFpt" alt="Logo" />
        <img :src="imgLogoUdpm" alt="Logo" />
      </div>
    </div>

    <div class="row">
      <h2 class="title">Đăng ký tài khoản sinh viên</h2>
      <div class="d-flex justify-content-center align-items-center">
        <div class="role-container">
          <a-form
            class="row"
            layout="vertical"
            autocomplete="off"
            :model="formData"
            @finish="handleSubmitRegister"
          >
            <a-form-item
              class="col-md-12"
              label="Cơ sở:"
              name="idFacility"
              :rules="formRules.idFacility"
            >
              <a-select
                v-model:value="formData.idFacility"
                class="w-100"
                :dropdownMatchSelectWidth="false"
                placeholder="-- Chọn 1 cơ sở--"
              >
                <a-select-option v-for="o in lstFacility" :value="o.id">{{
                  o.name
                }}</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item class="col-md-4" label="MSSV:" name="code" :rules="formRules.code">
              <a-input class="w-100" v-model:value="formData.code" allowClear />
            </a-form-item>
            <a-form-item class="col-md-8" label="Họ và tên:" name="name" :rules="formRules.name">
              <a-input class="w-100" v-model:value="formData.name" allowClear />
            </a-form-item>
            <a-form-item
              class="col-md-12"
              label="Xác thực khuôn mặt:"
              name="faceEmbedding"
              :rules="formRules.faceEmbedding"
            >
              <div class="face-id-input" @click="handleShowCamera">
                <svg
                  id="Layer_1"
                  style="enable-background: new 0 0 30 30"
                  version="1.1"
                  viewBox="0 0 30 30"
                  xml:space="preserve"
                  xmlns="http://www.w3.org/2000/svg"
                  xmlns:xlink="http://www.w3.org/1999/xlink"
                >
                  <path
                    d="  M12.062,20c0.688,0.5,1.688,1,2.938,1s2.25-0.5,2.938-1"
                    style="
                      fill: none;
                      stroke: #000000;
                      stroke-width: 2;
                      stroke-linecap: round;
                      stroke-linejoin: round;
                      stroke-miterlimit: 10;
                    "
                  />
                  <line
                    style="
                      fill: none;
                      stroke: #000000;
                      stroke-width: 2;
                      stroke-linecap: round;
                      stroke-linejoin: round;
                      stroke-miterlimit: 10;
                    "
                    x1="20"
                    x2="20"
                    y1="12"
                    y2="14"
                  />
                  <line
                    style="
                      fill: none;
                      stroke: #000000;
                      stroke-width: 2;
                      stroke-linecap: round;
                      stroke-linejoin: round;
                      stroke-miterlimit: 10;
                    "
                    x1="10"
                    x2="10"
                    y1="12"
                    y2="14"
                  />
                  <path
                    d="M15,12  v4c0,0.552-0.448,1-1,1"
                    style="
                      fill: none;
                      stroke: #000000;
                      stroke-width: 2;
                      stroke-linecap: round;
                      stroke-linejoin: round;
                      stroke-miterlimit: 10;
                    "
                  />
                  <g>
                    <path
                      d="M26,9   V6c0-1.105-0.895-2-2-2h-3"
                      style="
                        fill: none;
                        stroke: #000000;
                        stroke-width: 2;
                        stroke-linecap: round;
                        stroke-linejoin: round;
                        stroke-miterlimit: 10;
                      "
                    />
                    <path
                      d="M9,4   H6C4.895,4,4,4.895,4,6v3"
                      style="
                        fill: none;
                        stroke: #000000;
                        stroke-width: 2;
                        stroke-linecap: round;
                        stroke-linejoin: round;
                        stroke-miterlimit: 10;
                      "
                    />
                    <path
                      d="   M21,26h3c1.105,0,2-0.895,2-2v-3"
                      style="
                        fill: none;
                        stroke: #000000;
                        stroke-width: 2;
                        stroke-linecap: round;
                        stroke-linejoin: round;
                        stroke-miterlimit: 10;
                      "
                    />
                    <path
                      d="M4,21   v3c0,1.105,0.895,2,2,2h3"
                      style="
                        fill: none;
                        stroke: #000000;
                        stroke-width: 2;
                        stroke-linecap: round;
                        stroke-linejoin: round;
                        stroke-miterlimit: 10;
                      "
                    />
                  </g>
                </svg>
                <img :src="faceIDStore.dataImage" v-show="faceIDStore.dataImage != null" />
              </div>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" class="w-100" html-type="submit" @click="step1 = !step1"
                >Đăng ký tài khoản</a-button
              >
              <a-button class="w-100 mt-3" html-type="button" @click="handleLogout"
                >Huỷ bỏ</a-button
              >
            </a-form-item>
          </a-form>
        </div>
      </div>
      <p class="footer">Powered by <strong>FPLHN-UDPM</strong></p>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  margin-top: 6rem;
}
.logo {
  text-align: center;
}
.logo img {
  width: 200px;
  max-width: 100%;
  margin-bottom: 20px;
}
.title {
  margin: 2rem 0;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #41395a;
  text-transform: uppercase;
}
.role-container {
  max-width: 480px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #d6bfda;
  padding: 20px 40px;
  box-shadow: 4px 6px 1px 1px #efefef;
}
@media (max-width: 576px) {
  .role-container {
    padding: 20px 15px;
  }
}
.role-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}
.role-img {
  width: 280px;
  height: auto;
  max-width: 100%;
}
.role-button {
  width: 100%;
  margin-top: 10px;
  background-color: #41395b;
  border-color: #6b667d;
  color: white;
}
.role-button:hover,
.role-button:active {
  background-color: #6b667d;
  border-color: #6b667d;
  color: white;
}
.footer {
  margin-top: 6rem;
  font-size: 14px;
  color: gray;
  text-align: center;
}
.kt-divider {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-pack: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
}
.kt-divider > span:first-child {
  width: 100%;
  height: 1px;
  -webkit-box-flex: 1;
  -ms-flex: 1;
  flex: 1;
  background: #ebecf1;
  display: inline-block;
}
.kt-divider > span:last-child {
  width: 100%;
  height: 1px;
  -webkit-box-flex: 1;
  -ms-flex: 1;
  flex: 1;
  background: #ebecf1;
  display: inline-block;
}
.kt-divider > span:not(:first-child):not(:last-child) {
  padding: 0 2rem;
}
</style>
