package com.example.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.data.repository.CommentRepository
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.ProfileRepository
import com.example.app.domain.usecase.CreatePostUseCase
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.forgot_password.ForgotPasswordUiState
import com.example.app.ui.auth.forgot_password.ForgotPasswordViewModel
import com.example.app.ui.auth.forgot_password.components.ForgotEmailScreen
import com.example.app.ui.auth.forgot_password.components.ForgotNewPassWordScreen
import com.example.app.ui.auth.forgot_password.components.ForgotOtpScreen
import com.example.app.ui.auth.login.LoginScreen
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.auth.login.UpdateUsernameAccountScreen
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.register.components.RegisterDateScreen
import com.example.app.ui.auth.register.components.RegisterEmailScreen
import com.example.app.ui.auth.register.components.RegisterFullNameScreen
import com.example.app.ui.auth.register.components.RegisterOtpScreen
import com.example.app.ui.auth.register.components.RegisterPasswordScreen
import com.example.app.ui.chat.ChatScreen
import com.example.app.ui.chatlist.ChatListScreen
import com.example.app.ui.comment.CommentScreen
import com.example.app.ui.comment.CommentViewModel
import com.example.app.ui.comment.CommentViewModelFactory
import com.example.app.ui.feed.FeedUiState
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.create.CreatePostRoute
import com.example.app.ui.feed.create.CreatePostUiEvent
import com.example.app.ui.feed.create.CreatePostViewModel
import com.example.app.ui.feed.create.CreatePostViewModelFactory
import com.example.app.ui.feed.post.PostDetailScreen
import com.example.app.ui.feed.post.PostScreen
import com.example.app.ui.follow.FollowerFollowingViewModel
import com.example.app.ui.follow.FollowersFollowingScreen
import com.example.app.ui.main.MainScreen
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.ProfileUiState
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.profile.edit.EditProfileScreen
import com.example.app.ui.search.SearchHomeScreen
import com.example.app.ui.search.SearchResultScreen
import com.example.app.ui.search.SearchViewModel
import com.example.app.ui.share.SharePostScreen
import com.example.app.ui.share.SharePostViewModelFactory
import com.example.app.ui.welcome.WelcomeScreen
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.app.ui.groupchat.CreateGroupScreen
import com.example.app.ui.groupchat.GroupChatScreen
import com.example.app.ui.groupchat.GroupSettingsScreen
import com.example.app.ui.groupchat.AddMembersScreen
import com.example.app.ui.search.SearchUiState

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (AuthManager.isLoggedIn()) "main" else "welcome"
) {
    val forgotVm: ForgotPasswordViewModel = viewModel()
    val registerVm: RegisterViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {


        composable("welcome") {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    navController.navigate("register_fullName")
                }
            )
        }


        composable("register_fullName") {
            RegisterFullNameScreen(
                viewModel = registerVm,
                onClose = {
                    navController.popBackStack()
                },
                onNext = {
                    registerVm.nextStep()
                    navController.navigate("register_date")
                }
            )
        }


        composable("register_date") {
            RegisterDateScreen(
                viewModel = registerVm,
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    registerVm.nextStep()
                    navController.navigate("register_email")
                }
            )
        }


        composable("register_email") {
            RegisterEmailScreen(
                viewModel = registerVm,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate("register_password")
                }
            )
        }


        composable("register_password") {
            RegisterPasswordScreen(
                viewModel = registerVm,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate("register_otp")
                }
            )
        }


        composable("register_otp") {
            RegisterOtpScreen(
                viewModel = registerVm,
                onVerifySuccess = {
                    navController.navigate("login?reg_success=true") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
                onNoCode = {
                    registerVm.onNoCodeClick()
                    registerVm.resendOtp()
                }
            )
        }


        composable(
            route = "login?reg_success={reg_success}",
            arguments = listOf(
                navArgument("reg_success") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val regSuccess = backStackEntry.arguments?.getBoolean("reg_success") ?: false

            val loginViewModel: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = loginViewModel,
                successMessage = if (regSuccess) "Đăng ký thành công! Vui lòng đăng nhập." else null,
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate("welcome") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onLoginSuccess = {
                    if (AuthManager.isFirstLogin()) {
                        navController.navigate("update_username") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { navController.navigate("register_fullName") },
                onForgotPassWordActivity = { navController.navigate("forgot_email") }
            )
        }


        composable("update_username") {
            UpdateUsernameAccountScreen(
                onUpdateSuccess = {
                    navController.navigate("main") {
                        popUpTo("update_username") { inclusive = true }
                    }
                }
            )
        }


        composable("forgot_email") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.OtpSent) {
                    navController.navigate("forgot_otp")
                    forgotVm.clearState()
                }
            }

            ForgotEmailScreen(
                systemError = (uiState as? ForgotPasswordUiState.Error)?.message,
                isLoading = uiState is ForgotPasswordUiState.Loading,
                onBack = {
                    forgotVm.clearState()
                    navController.popBackStack()
                         },
                onNext = { email ->
                    forgotVm.sendOtp(email)
                },
                onErrorConsumed = {
                    forgotVm.clearState()
                }
            )
        }


        composable("forgot_otp") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.OtpVerified) {
                    navController.navigate("forgot_password_reset")
                    forgotVm.clearState()
                }
            }

            ForgotOtpScreen(
                systemError = (uiState as? ForgotPasswordUiState.Error)?.message,
                isLoading = uiState is ForgotPasswordUiState.Loading,
                onBack = {
                    forgotVm.clearState()
                    navController.popBackStack()
                         },
                onNext = { otp ->
                    forgotVm.verifyOtp(otp)
                },
                onNoCode = {
                    forgotVm.onNoCodeClick()
                    forgotVm.resendOtp()
                },
                viewModel = forgotVm
            )
        }


        composable("forgot_password_reset") {
            val uiState by forgotVm.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ForgotPasswordUiState.PasswordResetSuccess) {
                    navController.navigate("login") {
                        popUpTo("forgot_email") { inclusive = true }
                    }
                }
            }

            ForgotNewPassWordScreen(
                onBack = {
                    forgotVm.clearState()
                    navController.popBackStack()
                         },
                onDone = { newPass, confirmPass ->
                    forgotVm.resetPassword(newPass, confirmPass)
                }
            )
        }

        navigation(
            route = "main",
            startDestination = "home"
        ) {
            composable("home") { backStackEntry ->

                val feedViewModel: FeedViewModel = viewModel()
                val profileViewModel: ProfileViewModel = viewModel()

                val savedStateHandle = backStackEntry.savedStateHandle
                val updatedPostId by savedStateHandle.getLiveData<String>("updated_post_id")
                    .observeAsState()
                val updatedCount by savedStateHandle.getLiveData<Int>("updated_comment_count")
                    .observeAsState()

                LaunchedEffect(updatedPostId, updatedCount) {
                    if (updatedPostId != null && updatedCount != null) {
                        feedViewModel.updateCommentCount(updatedPostId!!, updatedCount!!)
                        profileViewModel.updateCommentCount(updatedPostId!!, updatedCount!!)
                        savedStateHandle.remove<String>("updated_post_id")
                        savedStateHandle.remove<Int>("updated_comment_count")
                    }
                }
                MainScreen(
                    rootNavController = navController,
                    profileViewModel = profileViewModel
                )
            }
            composable("chat_list") {
                ChatListScreen(
                    navController = navController
                )
            }
            composable("search") { backStackEntry ->
                val searchVm: SearchViewModel = viewModel(backStackEntry)

                SearchHomeScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = { userId ->
                        navController.navigate("profile/$userId")
                    },
                    onSubmit = { query ->
                        navController.navigate("search_result/$query")
                    },
                    viewModel = searchVm
                )
            }

            composable(
                route = "search_result/{query}",
                arguments = listOf(navArgument("query") { type = NavType.StringType })
            ) { backStackEntry ->

                val parentEntry1 = remember(backStackEntry) {
                    navController.getBackStackEntry("search")
                }

                val parentEntry2 = remember(backStackEntry) {
                    navController.getBackStackEntry("home")
                }

                val searchVm: SearchViewModel = viewModel(parentEntry1)
                val feedVm: FeedViewModel = viewModel(parentEntry2)

                val query = backStackEntry.arguments?.getString("query") ?: ""

                LaunchedEffect(query) {
                    searchVm.setQuery(query)
                }

                val savedStateHandle = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?: return@composable

                val updatedPostId by savedStateHandle
                    .getStateFlow("updated_post_id", "")
                    .collectAsState()

                val updatedCommentCount by savedStateHandle
                    .getStateFlow("updated_comment_count", -1)
                    .collectAsState()

                LaunchedEffect(updatedPostId, updatedCommentCount) {
                    if (updatedPostId.isNotEmpty() && updatedCommentCount >= 0) {
                        feedVm.updateCommentCount(updatedPostId, updatedCommentCount)

                        savedStateHandle.remove<String>("updated_post_id")
                        savedStateHandle.remove<Int>("updated_comment_count")
                    }
                }

                SearchResultScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = { userId ->
                        navController.navigate("profile/$userId")
                    },
                    onLikeClick = { postId ->
                        feedVm.toggleLike(postId)
                    },
                    onSaveClick = { postId ->
                        feedVm.toggleSave(postId)
                    },
                    onCommentClick = { postId ->
                        val post = (feedVm.uiState.value as? FeedUiState.Success)
                            ?.posts
                            ?.firstOrNull { it.id == postId }

                        val count = post?.commentCount ?: 0

                        navController.navigate("comments/$postId/$count")
                    },
                    onShareClick = { postId ->
                        navController.navigate("share_post/$postId")
                    },
                    viewModel = searchVm,
                    feedViewModel = feedVm,
                )
            }


            composable(
                // 1. Sửa route để nhận thêm tham số commentCount
                route = "comments/{postId}/{commentCount}",

                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType },
                    // 2. Khai báo tham số kiểu Int
                    navArgument("commentCount") {
                        type = NavType.IntType
                        defaultValue = 0 // Giá trị mặc định phòng hờ
                    }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                val initialCount = backStackEntry.arguments?.getInt("commentCount") ?: 0

                val repository = CommentRepository()

                val commentViewModel: CommentViewModel = viewModel(
                    factory = CommentViewModelFactory(repository)
                )

                CommentScreen(
                    postId = postId,
                    initialCommentCount = initialCount, // 4. Truyền vào Screen
                    onClose = {
                        val finalCount = commentViewModel.uiState.value.totalCount
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        android.util.Log.d("DEBUG_APP", "Đang đóng Comment. Người nhận là: $previousRoute")

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("updated_post_id", postId)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("updated_comment_count", finalCount)
                        navController.popBackStack()
                    },
                    viewModel = commentViewModel,
                    onProfileClick = { targetId ->
                        navController.navigate("profile/$targetId")
                    }
                )
            }



            composable(
                route = "profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"

                val profileViewModel: ProfileViewModel = viewModel()
                val feedVm: FeedViewModel = viewModel()

                // 👇 1. THÊM ĐOẠN CODE HỨNG DỮ LIỆU NÀY (giống hệt bên Home)
                val savedStateHandle = backStackEntry.savedStateHandle
                val updatedPostId by savedStateHandle.getLiveData<String>("updated_post_id")
                    .observeAsState()
                val updatedCount by savedStateHandle.getLiveData<Int>("updated_comment_count")
                    .observeAsState()

                LaunchedEffect(updatedPostId, updatedCount) {
                    if (updatedPostId != null && updatedCount != null) {
                        profileViewModel.updateCommentCount(updatedPostId!!, updatedCount!!)
                        savedStateHandle.remove<String>("updated_post_id")
                        savedStateHandle.remove<Int>("updated_comment_count")
                    }
                }

                ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    userId = userId,
                    feedViewModel = feedVm
                )
            }


            composable("edit_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val context = LocalContext.current

                val mainEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }

                val profileViewModel: ProfileViewModel = viewModel(mainEntry) // ✅ CHUNG
                val feedViewModel: FeedViewModel =
                    viewModel(mainEntry)       // ✅ CHUNG (thêm dòng này)

                val editState by profileViewModel.editState.collectAsState()

                LaunchedEffect(userId) {
                    profileViewModel.loadUserDetailForEdit(userId)
                }

                EditProfileScreen(
                    navController = navController,
                    uiState = editState,
                    onBack = { navController.popBackStack() },
                    onUsernameChange = profileViewModel::onEditUsernameChange,
                    onFullNameChange = profileViewModel::onEditFullNameChange,
                    onGenderChange = profileViewModel::onEditGenderChange,
                    onAddressChange = profileViewModel::onEditAddressChange,
                    onPhoneChange = profileViewModel::onEditPhoneChange,
                    onAvatarPicked = profileViewModel::onEditAvatarPicked,
                    onSubmit = {
                        profileViewModel.submitEditProfile(context) {
                            val s0 = profileViewModel.editState.value
                            feedViewModel.updateMyProfileInFeed(
                                newUsername = s0.username.trim(),
                                newFullName = s0.fullName.trim(),
                                newAvatarUrl = s0.avatarUrl
                            )
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("PROFILE_UPDATED", true)

                            navController.popBackStack()
                        }
                    }
                )
            }


            composable(
                route = "followers/{userId}?tab={tab}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("tab") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                val followViewModel: FollowerFollowingViewModel = viewModel()
                val scope = rememberCoroutineScope()
                val repo = remember { ProfileRepository() }
                FollowersFollowingScreen(
                    userId = userId,
                    initialTab = initialTab,
                    viewModel = followViewModel,
                    onBack = { navController.popBackStack() },
                    onProfileClick = { clickedUserId ->
                        navController.navigate("profile/$clickedUserId")
                    },
                    onUnfollow = { id ->
                        scope.launch {
                            repo.unfollowUser(id)
                        }
                    }
                )
            }


            // ✅ ============ GROUP CHAT ROUTES ============

            // Tạo nhóm mới
            composable("create_group") {
                CreateGroupScreen(navController)
            }

            // Chat trong nhóm
            composable("group_chat/{conversationId}") { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")!!
                GroupChatScreen(conversationId, navController)
            }

            // Cài đặt nhóm
            composable("group_settings/{conversationId}") { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")!!
                GroupSettingsScreen(conversationId, navController)
            }

            // Thêm thành viên
            composable("add_members/{conversationId}") { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")!!
                AddMembersScreen(conversationId, navController)
            }


            composable(
                route = "share_post/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                if (postId.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy bài viết để chia sẻ")
                    }
                    return@composable
                }

                val mainEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }
                val profileViewModel: ProfileViewModel = viewModel(mainEntry)
                val profileState by profileViewModel.uiState.collectAsState()
                val currentProfile = (profileState as? ProfileUiState.Success)?.profile

                val createVm: CreatePostViewModel = viewModel(
                    key = "share_post_$postId",  // key ổn định dựa trên postId
                    factory = SharePostViewModelFactory(
                        createPostUseCase = CreatePostUseCase(PostRepository()),
                        avatarUrl = currentProfile?.avatarUrl,
                        userName = currentProfile?.fullName ?: "Bạn"
                    )
                )

                LaunchedEffect(postId) {
                    createVm.initShareMode(postId)  // gọi hàm với postId (String)
                }

                LaunchedEffect(Unit) {
                    createVm.events.collect { event ->
                        if (event is CreatePostUiEvent.Close) {
                            navController.popBackStack()
                        }
                    }
                }

                val state by createVm.uiState.collectAsState()
                SharePostScreen(
                    state = state,
                    onAction = createVm::onAction
                )
            }

            composable(
                route = "post/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                PostScreen(
                    postId = postId,
                    navController = navController
                )
            }

            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val commentRepository = remember { CommentRepository() }
                val commentViewModel: CommentViewModel = viewModel(
                    factory = CommentViewModelFactory(commentRepository)
                )
                val feedViewModel: FeedViewModel = viewModel()

                PostDetailScreen(
                    postId = postId,
                    navController = navController,
                    commentViewModel = commentViewModel,
                    feedViewModel = feedViewModel
                )
            }


            composable("create_post") { backStackEntry ->
                val mainEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }

                val profileViewModel: ProfileViewModel = viewModel(mainEntry)
                val profileState by profileViewModel.uiState.collectAsState()

                val me = (profileState as? ProfileUiState.Success)?.profile
                val avatarUrl = me?.avatarUrl
                val userName = me?.fullName ?: "Bạn"

                val createVm: CreatePostViewModel = viewModel(
                    factory = CreatePostViewModelFactory(
                        createPostUseCase = CreatePostUseCase(PostRepository()),
                        avatarUrl = avatarUrl,
                        userName = userName
                    )
                )

                CreatePostRoute(
                    viewModel = createVm,
                    onClose = { navController.popBackStack() },
                    onSubmit = { content, privacy, mediaItems ->
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "chat/{userId}?name={name}&avatar={avatar}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                    navArgument("avatar") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val otherUserId = backStackEntry.arguments?.getString("userId") ?: return@composable
                val otherName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("name").orEmpty(),
                    StandardCharsets.UTF_8.toString()
                )

                val otherAvatar = URLDecoder.decode(
                    backStackEntry.arguments?.getString("avatar").orEmpty(),
                    StandardCharsets.UTF_8.toString()
                ).ifBlank { null }

                ChatScreen(
                    otherUserId = otherUserId,
                    otherName = otherName,
                    otherAvatarUrl = otherAvatar,
                    navController = navController
                )
            }
        }
    }
}