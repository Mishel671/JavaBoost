package com.dzyuba.javaboost.presentation.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.dzyuba.javaboost.App
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.FragmentProfileBinding
import com.dzyuba.javaboost.domain.entities.User
import com.dzyuba.javaboost.presentation.ViewModelFactory
import com.dzyuba.javaboost.presentation.adapter.BaseAdapter
import com.dzyuba.javaboost.presentation.adapter.BaseItem
import com.dzyuba.javaboost.presentation.adapter.elements.ItemDividerBig
import com.dzyuba.javaboost.presentation.adapter.elements.ItemGrayLine
import com.dzyuba.javaboost.presentation.adapter.elements.ItemHeader
import com.dzyuba.javaboost.presentation.adapter.elements.ItemTwoTextWithArrow
import com.dzyuba.javaboost.presentation.lessons.LessonsListFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment.Companion.EDIT_RESULT_KEY
import com.dzyuba.javaboost.presentation.nickname.NicknameFragment.Companion.launchEditMode
import com.dzyuba.javaboost.presentation.signin.SignInFragment
import com.dzyuba.javaboost.util.*
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")

    private val dialog by lazy { initProgressBar(layoutInflater, requireContext()) }
    private val baseAdapter = BaseAdapter()

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val uri = it.data?.data
                uri?.let {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(
                            requireActivity().contentResolver,
                            uri
                        )
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        getBitmap(
                            requireActivity().contentResolver,
                            uri
                        )
                    }
                    viewModel.updateImage(bitmap)
                }

            }
        }

    private val component by lazy {
        (requireActivity().application as App).componentApp
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setFragmentsResult()
    }

    override fun onStart() {
        showNavBar()
        super.onStart()
    }

    private fun setFragmentsResult() {
//        parentFragmentManager.setFragmentResultListener(
//            EDIT_RESULT_KEY,
//            viewLifecycleOwner
//        ) { key, bundle ->
//            showNavBar()
//        }
    }

    private fun setObservers() {
        viewModel.profile.observe(viewLifecycleOwner) {
            it.ifLoading {
                binding.root.isRefreshing = false
                dialog.show()
            }.ifError {
                binding.root.isRefreshing = false
                dialog.dismiss()
                showErrorAlert(it)
            }.ifSuccess { user ->
                binding.root.isRefreshing = false
                dialog.dismiss()
                user?.let {
                    Log.d("MainLog", "Logo: ${user.photoUrl}")
                    setUI(user)
                }
            }
        }
    }

    private fun setUI(user: User) {
        binding.ivLogo.loadCircularImage(user.photoUrl)
        binding.tvNickname.text = user.name ?: getString(R.string.unknown_value)
        binding.tvEmail.text = user.email
        binding.ivEdit.setOnClickListener {
            launchScreenReturn(NicknameFragment.launchEditMode())
        }
        binding.root.setOnRefreshListener {
            viewModel.loadProfile()
        }
        baseAdapter.reset()
        baseAdapter.items = getItems(user)
        baseAdapter.installOn(binding.llProfile, layoutInflater)

    }

    private fun getItems(user: User): ArrayList<BaseItem> = ArrayList<BaseItem>().apply {

        add(ItemHeader().apply {
            headerText = getString(R.string.profile_lessons)
        })
        add(ItemTwoTextWithArrow().apply {
            leftText = getString(R.string.profile_learned_lessons)
            positionType = ItemTwoTextWithArrow.PositionType.SOLO
            onItemClick = {
                launchScreenReturn(LessonsListFragment.lessonsById(listOf(1, 2)))
            }
        })
        add(ItemDividerBig())
        add(ItemHeader().apply {
            headerText = getString(R.string.profile_settings)
        })
        add(ItemTwoTextWithArrow().apply {
            leftText = getString(R.string.profile_change_nickname)
            positionType = ItemTwoTextWithArrow.PositionType.TOP
            onItemClick = {
                launchScreenReturn(NicknameFragment.launchEditMode())
            }
        })
        add(ItemGrayLine())
        add(ItemTwoTextWithArrow().apply {
            leftText = getString(R.string.profile_change_photo)
            positionType = ItemTwoTextWithArrow.PositionType.MIDDLE
            onItemClick = {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryResult.launch(intent)
            }
        })
        add(ItemGrayLine())
        add(ItemTwoTextWithArrow().apply {
            leftText = getString(R.string.profile_quit)
            positionType = ItemTwoTextWithArrow.PositionType.BOTTOM
            onItemClick = {
                showAlert(
                    titleResId = R.string.profile_quit_title,
                    message = getString(R.string.profile_quit_desc),
                    positiveResId = R.string.accept,
                    negativeResId = R.string.cancel,
                    positiveAction = {
                        viewModel.logout()
                        startSignInScreen()
                    }
                )
            }
        })
    }

    private fun launchScreenReturn(fragment: Fragment) {
        hideNavBar()
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.slide_enter_left,
                R.anim.slide_exit_left,
                R.anim.slide_enter_right,
                R.anim.slide_exit_right
            )
            .replace(R.id.bnvFragmentContainer, fragment)
            .commit()
    }

    private fun startSignInScreen() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_enter_right, R.anim.slide_exit_right)
            .replace(R.id.fragmentContainer, SignInFragment.newInstance())
            .commit()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {

        fun newInstance() = ProfileFragment()
    }

}