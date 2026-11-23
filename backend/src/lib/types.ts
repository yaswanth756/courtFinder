export interface UserDto {
  id: string;
  name: string;
  email: string;
  isGuest?: boolean;
}

export interface AuthResponseDto {
  success: boolean;
  token?: string | null;
  user?: UserDto | null;
  errorMessage?: string | null;
}

export interface CourtDto {
  id: string;
  name: string;
  sportType: string;
  location: string;
  distanceKm: number;
  latitude: number;
  longitude: number;
  pricePerHour: string;
  rating: number;
  availableTimeSlots: string[];
  aiMatchScore: number;
  isFavorite?: boolean;
  hasAlertSet?: boolean;
  amenities: string[];
}

export interface BookingRequestDto {
  courtId: string;
  timeSlot: string;
}

export interface BookingConfirmationDto {
  bookingId: string;
  courtName: string;
  timeSlot: string;
  date: string;
}

export interface ApiErrorDto {
  errorMessage: string;
}
