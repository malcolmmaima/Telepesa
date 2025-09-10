import { useAuth } from '../store/auth'

export function HomePage() {
  const { user } = useAuth()
  return (
    <div className="min-h-screen bg-[#f6f9fb]">
      <header className="px-6 py-4 border-b bg-white">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="text-[#0b3b5e] font-semibold">Telepesa</div>
          <div className="text-[#5b7083] text-sm">{user?.email}</div>
        </div>
      </header>
      <main className="max-w-5xl mx-auto p-6">
        <div className="rounded-2xl border bg-white p-6">
          <h2 className="text-xl font-medium text-[#0b3b5e]">Welcome back{user ? `, ${user.firstName ?? user.username}` : ''}</h2>
          <p className="text-[#5b7083] mt-2">Balances, accounts, and quick actions will appear here.</p>
        </div>
      </main>
    </div>
  )
}


