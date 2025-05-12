import axios, {type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from "axios";
import { message } from "ant-design-vue";
// 创建 Axios 实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8100/',
  timeout:  60000,// 请求超时时间
  withCredentials: true,
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response;
    // 请求未授权
    if (data.code === 40100) {
      // 不在获取用户信息的请求，并且用户不在登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes("/user/get/login") &&
        !window.location.pathname.includes("/user/login")
      ) {
        message.warning({ content: "请先登录" });
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
    }
    return response;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  }
);

// 统一封装请求函数
export const index = <T = any>(
  config: InternalAxiosRequestConfig
): Promise<T> => {
  return service(config);
};

export default service;
