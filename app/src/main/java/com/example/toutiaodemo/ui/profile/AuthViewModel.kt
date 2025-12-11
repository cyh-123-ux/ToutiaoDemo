package com.example.toutiaodemo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class User(
    val isLogin: Boolean = false,
    val username: String = "",
    val phone: String = "",
    val avatar: String = "https://tva1.sinaimg.cn/crop.0.0.1080.1080.180/006y8mN6ly1g6e2tdgve1j30u00u0n0j.jpg",
    val collectCount: Int = 0,
    val readCount: Int = 0
)
//登陆状态ui模型
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMsg: String?=null,
    val currentUser: User = User()
)

//核心AuthViewModel类
class AuthViewModel : ViewModel(){
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMsg = null) }
            delay(1000) // 模拟网络请求

            // 恢复原有赋值逻辑（确保字段有值，能显示）
            if (phone.isNotEmpty() && password.isNotEmpty()) {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = User(
                            isLogin = true,
                            username = "头条用户$phone",
                            phone = phone,
                            collectCount = (10..50).random(),
                            readCount = (100..500).random()
                        )
                    )
                }
            } else {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "手机号或密码不能为空"
                    )
                }
            }
        }
    }

    //模拟注册
    fun register(username: String,phone: String,password: String){
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMsg = null) }
            delay(1000)
            if(username.isNotEmpty() && phone.isNotEmpty() && password.length>=6){
                _authState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = User(
                            isLogin = true,
                            username = username,
                            phone = phone,
                            collectCount = 0,
                            readCount = 0
                        )
                    )
                }
            }else{
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = when{
                            username.isEmpty() -> "用户名不能为空"
                            phone.isEmpty() -> "手机号不能为空"
                            else -> "密码长度不能少于6位"
                        }
                    )
                }
            }
        }
    }
    //退出登录
    fun logout(){
        _authState.update { it.copy(currentUser = User()) }
    }
}