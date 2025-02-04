// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClicked = { task ->
                showTaskDialog(task)
            },
            onDeleteClicked = { task ->
                viewModel.delete(task)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allTasks.collect { tasks ->
                    adapter.submitList(tasks)
                }
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showTaskDialog()
        }
    }

    private fun showTaskDialog(task: Task? = null) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (task == null) "New Task" else "Edit Task")
            .setView(R.layout.dialog_task)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val titleInput = dialog.findViewById<EditText>(R.id.titleInput)
            val descriptionInput = dialog.findViewById<EditText>(R.id.descriptionInput)
            
            task?.let {
                titleInput.setText(it.title)
                descriptionInput.setText(it.description)
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text.toString().trim()
                if (title.isEmpty()) {
                    titleInput.error = "Title required"
                    return@setOnClickListener
                }

                val newTask = task?.copy(
                    title = title,
                    description = descriptionInput.text.toString().trim()
                ) ?: Task(title = title, description = descriptionInput.text.toString().trim())

                viewModel.insert(newTask)
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
