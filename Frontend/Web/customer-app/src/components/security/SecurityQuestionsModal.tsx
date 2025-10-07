import { useState, useEffect } from 'react'
import { Button } from '../ui/Button'
import { Input } from '../ui/Input'
import { Card } from '../ui/Card'
import { securityApi, type SecurityQuestion, DEFAULT_SECURITY_QUESTIONS } from '../../api/security'

interface SecurityQuestionsModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
  mode: 'setup' | 'verify' | 'update'
  title?: string
  description?: string
}

export function SecurityQuestionsModal({
  isOpen,
  onClose,
  onSuccess,
  mode,
  title,
  description,
}: SecurityQuestionsModalProps) {
  const [questions, setQuestions] = useState<SecurityQuestion[]>([])
  const [selectedQuestions, setSelectedQuestions] = useState<number[]>([])
  const [answers, setAnswers] = useState<{ [key: number]: string }>({})
  const [userQuestions, setUserQuestions] = useState<any[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState(1) // 1: select questions, 2: provide answers

  useEffect(() => {
    if (isOpen) {
      loadData()
    }
  }, [isOpen, mode])

  const loadData = async () => {
    try {
      if (mode === 'setup' || mode === 'update') {
        // Use default questions for now
        setQuestions(DEFAULT_SECURITY_QUESTIONS)
      } else if (mode === 'verify') {
        // Load user's security questions
        const userQs = await securityApi.getUserSecurityQuestions()
        setUserQuestions(userQs)
      }
    } catch (err: any) {
      console.error('Failed to load security questions:', err)
      // Use default questions as fallback
      setQuestions(DEFAULT_SECURITY_QUESTIONS)
    }
  }

  const handleQuestionSelect = (questionId: number) => {
    if (selectedQuestions.includes(questionId)) {
      setSelectedQuestions(prev => prev.filter(id => id !== questionId))
      setAnswers(prev => {
        const newAnswers = { ...prev }
        delete newAnswers[questionId]
        return newAnswers
      })
    } else if (selectedQuestions.length < 3) {
      setSelectedQuestions(prev => [...prev, questionId])
    }
  }

  const handleAnswerChange = (questionId: number, answer: string) => {
    setAnswers(prev => ({ ...prev, [questionId]: answer }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (mode === 'setup' || mode === 'update') {
        if (step === 1) {
          if (selectedQuestions.length !== 3) {
            setError('Please select exactly 3 security questions')
            return
          }
          setStep(2)
          return
        }

        // Step 2: Submit answers
        const allAnswered = selectedQuestions.every(id => answers[id]?.trim())
        if (!allAnswered) {
          setError('Please answer all selected questions')
          return
        }

        const request = {
          questions: selectedQuestions.map(id => ({
            questionId: id,
            answer: answers[id].trim().toLowerCase(),
          })),
        }

        if (mode === 'setup') {
          await securityApi.setSecurityQuestions(request)
        } else {
          await securityApi.updateSecurityQuestions(request)
        }
      } else if (mode === 'verify') {
        const allAnswered = userQuestions.every(q => answers[q.questionId]?.trim())
        if (!allAnswered) {
          setError('Please answer all security questions')
          return
        }

        const request = {
          answers: userQuestions.map(q => ({
            questionId: q.questionId,
            answer: answers[q.questionId].trim().toLowerCase(),
          })),
        }

        const result = await securityApi.verifySecurityQuestions(request)
        if (!result.valid) {
          setError('One or more answers are incorrect')
          return
        }
      }

      onSuccess()
      handleClose()
    } catch (err: any) {
      setError(err.message || 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setSelectedQuestions([])
    setAnswers({})
    setUserQuestions([])
    setError('')
    setLoading(false)
    setStep(1)
    onClose()
  }

  const getTitle = () => {
    if (title) return title
    switch (mode) {
      case 'setup':
        return '🛡️ Setup Security Questions'
      case 'update':
        return '🔄 Update Security Questions'
      case 'verify':
        return '🔐 Verify Identity'
      default:
        return 'Security Questions'
    }
  }

  const getDescription = () => {
    if (description) return description
    switch (mode) {
      case 'setup':
        return 'Choose 3 security questions to help protect your account'
      case 'update':
        return 'Update your security questions for better account protection'
      case 'verify':
        return 'Answer your security questions to verify your identity'
      default:
        return ''
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="text-center mb-6">
            <h2 className="text-xl font-bold text-financial-navy mb-2">{getTitle()}</h2>
            <p className="text-financial-gray text-sm">{getDescription()}</p>
            {(mode === 'setup' || mode === 'update') && step === 1 && (
              <p className="text-financial-gray text-xs mt-2">
                Step 1 of 2: Select 3 questions ({selectedQuestions.length}/3 selected)
              </p>
            )}
            {(mode === 'setup' || mode === 'update') && step === 2 && (
              <p className="text-financial-gray text-xs mt-2">Step 2 of 2: Provide your answers</p>
            )}
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-600 text-sm flex items-center">
                <span className="mr-2">⚠️</span>
                {error}
              </p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Question Selection (Setup/Update Step 1) */}
            {(mode === 'setup' || mode === 'update') && step === 1 && (
              <div className="space-y-3">
                {questions.map(question => (
                  <div
                    key={question.id}
                    onClick={() => handleQuestionSelect(question.id)}
                    className={`p-4 border rounded-lg cursor-pointer transition-all ${
                      selectedQuestions.includes(question.id)
                        ? 'border-financial-blue bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    } ${
                      !selectedQuestions.includes(question.id) && selectedQuestions.length >= 3
                        ? 'opacity-50 cursor-not-allowed'
                        : ''
                    }`}
                  >
                    <div className="flex items-center">
                      <div
                        className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${
                          selectedQuestions.includes(question.id)
                            ? 'border-financial-blue bg-financial-blue'
                            : 'border-gray-300'
                        }`}
                      >
                        {selectedQuestions.includes(question.id) && (
                          <svg
                            className="w-3 h-3 text-white"
                            fill="currentColor"
                            viewBox="0 0 20 20"
                          >
                            <path
                              fillRule="evenodd"
                              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                              clipRule="evenodd"
                            />
                          </svg>
                        )}
                      </div>
                      <div>
                        <p className="font-medium text-financial-navy">{question.question}</p>
                        <p className="text-xs text-financial-gray capitalize">
                          {question.category}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Answer Input (Setup/Update Step 2) */}
            {(mode === 'setup' || mode === 'update') && step === 2 && (
              <div className="space-y-4">
                {selectedQuestions.map(questionId => {
                  const question = questions.find(q => q.id === questionId)
                  return (
                    <div key={questionId}>
                      <label className="block text-sm font-medium text-financial-navy mb-2">
                        {question?.question}
                      </label>
                      <Input
                        value={answers[questionId] || ''}
                        onChange={e => handleAnswerChange(questionId, e.target.value)}
                        placeholder="Enter your answer"
                        required
                      />
                    </div>
                  )
                })}
              </div>
            )}

            {/* Verification Mode */}
            {mode === 'verify' && (
              <div className="space-y-4">
                {userQuestions.map(userQuestion => (
                  <div key={userQuestion.id}>
                    <label className="block text-sm font-medium text-financial-navy mb-2">
                      {userQuestion.question}
                    </label>
                    <Input
                      value={answers[userQuestion.questionId] || ''}
                      onChange={e => handleAnswerChange(userQuestion.questionId, e.target.value)}
                      placeholder="Enter your answer"
                      required
                    />
                  </div>
                ))}
              </div>
            )}

            <div className="flex gap-3 pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={handleClose}
                disabled={loading}
                className="flex-1"
              >
                Cancel
              </Button>
              {(mode === 'setup' || mode === 'update') && step === 1 && (
                <Button
                  type="submit"
                  disabled={loading || selectedQuestions.length !== 3}
                  className="flex-1"
                >
                  Next
                </Button>
              )}
              {((mode === 'setup' || mode === 'update') && step === 2) ||
                (mode === 'verify' && (
                  <Button type="submit" disabled={loading} className="flex-1">
                    {loading ? (
                      <>
                        <svg
                          className="animate-spin -ml-1 mr-2 h-4 w-4"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          ></circle>
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          ></path>
                        </svg>
                        Processing...
                      </>
                    ) : mode === 'verify' ? (
                      'Verify'
                    ) : (
                      'Save Questions'
                    )}
                  </Button>
                ))}
            </div>
          </form>

          {mode === 'setup' && (
            <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-blue-700 text-xs">
                <strong>Security Tips:</strong>
                <br />• Choose questions only you would know the answer to
                <br />• Use answers that won't change over time
                <br />• Remember your answers exactly as you type them
                <br />• Keep your answers confidential
              </p>
            </div>
          )}
        </div>
      </Card>
    </div>
  )
}
