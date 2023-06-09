package com.dzyuba.javaboost.presentation.splash

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentSplashBinding
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.main.MainFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.presentation.signin.SignInFragment
import com.dzyuba.javaboost.util.showErrorAlert
import javax.inject.Inject

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding: FragmentSplashBinding
        get() = _binding ?: throw RuntimeException("FragmentSplashBinding == null")
    private var animPlayed = false

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[SplashViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setUI()
    }

    private fun setUI() {
        binding.loadingAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {
                if (viewModel.needScreen.value != null) {
                    launchNeedFragment(viewModel.needScreen.value!!)
                    binding.loadingAnim.pauseAnimation()
                } else {
                    animPlayed = true
                }
            }

        })
    }

    private fun setObservers() {
        viewModel.needScreen.observe(viewLifecycleOwner) {
            if (animPlayed == true) {
                launchNeedFragment(it)
            }
        }
    }

    private fun launchNeedFragment(needScreen: NeedScreen) {
        when (needScreen) {
            NeedScreen.SIGN_IN -> {
                launchFragment(SignInFragment.newInstance(), true)
            }
            NeedScreen.EMAIL_VERIFIED -> {
                launchFragment(SignInFragment.newInstanceToEmailVerified(), false,)
            }
            NeedScreen.NICKNAME -> {
                launchFragment(NicknameFragment.launchRegistrationMode(), true)
            }
            NeedScreen.MAIN_SCREEN -> {
                launchFragment(MainFragment.newInstance(), true)
            }
            NeedScreen.ERROR -> {
                showErrorAlert(needScreen.error ?: Throwable("Unknown error"), positiveAction = {
                    viewModel.checkAuthentication()
                    binding.loadingAnim.playAnimation()
                })
            }
        }

    }

    private fun launchFragment(fragment: Fragment, withAnimation: Boolean) {
        parentFragmentManager.beginTransaction().apply {
            if (withAnimation) {
                setCustomAnimations(R.anim.slide_enter_left, R.anim.slide_exit_left)
            }
            replace(R.id.fragmentContainer, fragment)

        }.commit()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = SplashFragment()
    }
}