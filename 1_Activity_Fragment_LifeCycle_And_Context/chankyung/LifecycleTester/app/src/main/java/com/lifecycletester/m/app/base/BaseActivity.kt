package com.lifecycletester.m.app.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.lifecycletester.m.app.R
import com.lifecycletester.m.app.util.ActivityLifecycleObserver
import com.lifecycletester.m.app.util.ActivityUtil
import com.lifecycletester.m.app.util.OnBackPressedListener
import com.orhanobut.logger.Logger

abstract class BaseActivity<DB : ViewDataBinding> : AppCompatActivity(), BaseNavigator
{
    /********************************************************/
    lateinit var mBinding: DB
    private val m_lifecycleObserver = ActivityLifecycleObserver(lifecycle)
    /********************************************************/

    @LayoutRes
    abstract fun getLayoutResourceId(): Int

    /**
     * data binding 설정.
     */
    abstract fun initDataBinding()

    /**
     * activity, window 속성 초기화.
     * 초기 Fragment 설정.
     * BottomNavigationView, RecyclerView 등 초기화
     */
    abstract fun initView()

    override fun getContext(): Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, getLayoutResourceId())
        lifecycle.addObserver(m_lifecycleObserver)

        initDataBinding()
        initView()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    fun <T : Fragment> findOrCreateFragment(type: Class<T>, _param: Parcelable? = null, frameResId : Int): T{
        //val frameLayoutResId: Int = R.id.content

        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(frameResId)

        return when{
            isFinishing -> currentFragment as T

            currentFragment == type -> currentFragment as T

            currentFragment == null -> {
                val instance: T = type.newInstance()
                _param?.let {
                    instance.arguments = Bundle(1).apply { putParcelable("data", _param) }
                }

                ActivityUtil.addFragmentToActivity(fragmentManager, instance, frameResId)
                instance
            }

            currentFragment.javaClass == type -> currentFragment as T

            else -> {
                val instance: T = type.newInstance()
                _param?.let {
                    instance.arguments = Bundle(1).apply { putParcelable("data", _param) }
                }

                ActivityUtil.replaceFragmentToActivity(fragmentManager, instance, frameResId)
                instance
            }
        }
    }

    fun <T : Fragment> createFragment(type: Class<T>, _param: Parcelable? = null, frameResId: Int = R.id.content): T {
        val fragmentManager = supportFragmentManager

        val instance: T = type.newInstance()
        _param?.let {
            instance.arguments = Bundle(1).apply { putParcelable("data", _param) }
        }

        ActivityUtil.addFragmentToActivity(fragmentManager, instance, frameResId)

        return instance
    }

    protected fun removeFragment(fragment: Fragment?) {
        fragment?.let {
            ActivityUtil.delFragmentToActivity(supportFragmentManager, it)
        }
    }

    override fun finishActivity(_isMain: Boolean) {
        if (!isFinishing){
            if(_isMain){
                moveTaskToBack(true)
                finishAndRemoveTask()
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            else{
                finish()
            }
        }
    }

    override fun showToast(_msg: String) {
        Toast.makeText(this, _msg, Toast.LENGTH_LONG).show()
    }

    fun popFragment(frameResId : Int){
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(frameResId)

        Logger.i("popFragment :: ${fragmentManager.backStackEntryCount}")

        fragmentManager.popBackStack()

        //removeFragment(currentFragment)
    }

    private var mBackPressedListener: OnBackPressedListener? = null
    override fun setOnBackButtonTouchListener(listener: OnBackPressedListener) {
        mBackPressedListener = listener
    }

    override fun removeOnBackButtonTouchListener() {
        mBackPressedListener = null
    }

    override fun onBackPressed() {
        mBackPressedListener?.let{
            it.onBackPressed()

            if(it.onSuperBackPressed) super.onBackPressed()
        } ?: run{
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Logger.i("onConfigurationChanged")
    }
}